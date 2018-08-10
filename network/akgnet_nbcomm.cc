/*
* This file is part of rasdaman community.
*
* Rasdaman community is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Rasdaman community is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
*
* Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
rasdaman GmbH.
*
* For more information please see <http://www.rasdaman.org>
* or contact Peter Baumann via <baumann@rasdaman.com>.
*/
/**
 * SOURCE: akgnet_nbcomm.cc
 *
 * MODULE: akg network
 * CLASS:  NbJob, NbServerJob, NbClientJob, NbCommunicator
 *
 * COMMENTS:
 *
*/

#include "config.h"
#include <akgnet_nbcomm.hh>
#include <assert.h>
#include <logging.hh>

//### NBJob - static members #########################
time_t akg::NbJob::timeOutInterv = 30;
time_t akg::NbJob::currentTime = 0;

void akg::NbJob::setCurrentTime() noexcept
{
    currentTime = time(NULL);
}

void akg::NbJob::setTimeoutInterval(time_t x) noexcept
{
    timeOutInterv = x;
}

time_t akg::NbJob::getTimeoutInterval() noexcept
{
    return timeOutInterv;
}

//####################################################
akg::NbJob::NbJob(FileDescriptor& fd) noexcept
    : fdRef(fd)
{
    status           = wks_notdefined;
    selectorPtr      = NULL;
    currentBufferPtr = NULL;
    lastActionTime   = 0;
}

akg::NbJob::~NbJob() noexcept
{
}

akg::NbJob::workingStatus akg::NbJob::getStatus() noexcept
{
    return status;
}

bool akg::NbJob::isOperationPending() noexcept
{
    return (status != wks_notdefined &&
            status != wks_accepting) ? true : false;
}

bool akg::NbJob::isAccepting() noexcept
{
    return status == wks_accepting ? true : false;
}
bool akg::NbJob::isReading() noexcept
{
    return status == wks_reading ? true : false;
}
bool akg::NbJob::isWriting() noexcept
{
    return status == wks_writing ? true : false;
}
bool akg::NbJob::isProcessing() noexcept
{
    return status == wks_processing ? true : false;
}

bool akg::NbJob::readPartialMessage() noexcept
{
    assert(currentBufferPtr != NULL);

    action();

    int nbytes = currentBufferPtr->read(fdRef);

    if (nbytes > 0)
    {
        LDEBUG << "..read socket(" << fdRef() << ") " << nbytes;
        return validateMessage();
    }

    else
    {
        int saveerrno = fdRef.getErrno();
        switch (saveerrno)
        {
        case EINTR:  //LDEBUG << "EINTR, retry please";
            break;

        case EAGAIN: //LDEBUG << "EAGAIN, retry please";
            break;

        //case 0:      LDEBUG << "Premature End-of-file";
        // executeOnReadError() ???
        //       break;

        default:
            LDEBUG << "Read error " << saveerrno;
            executeOnReadError();
            break;
        }
    }
    return false;
}

bool akg::NbJob::writePartialMessage() noexcept
{
    assert(currentBufferPtr != NULL);

    action();
    int nbytes = currentBufferPtr->write(fdRef);

    if (nbytes > 0)
    {
        LDEBUG << "..write socket(" << fdRef() << ") " << nbytes;

        if (currentBufferPtr->getNotSendedSize() == 0)
        {
            LDEBUG << "Write ready";
            executeOnWriteReady();
            return true;
        }
    }
    else
    {
        int saveerrno = fdRef.getErrno();
        switch (saveerrno)
        {
        case EINTR:  //LDEBUG << "EINTR, retry please";
            break;

        case EAGAIN: //LDEBUG << "EAGAIN, retry please";
            break;

        //case 0:      LDEBUG << "Premature partner hang up"; //?? valabil la write
        //       break;

        default:
            LDEBUG << "Write error " << saveerrno;
            executeOnWriteError();
            break;
        }
    }
    return  false;
}

bool akg::NbJob::cleanUpIfTimeout() noexcept
{
    if (fdRef.isOpen() == false)
    {
        return false;
    }
    if (lastActionTime + timeOutInterv > currentTime)
    {
        return false;
    }

    LDEBUG << "Client socket " << fdRef() << " timeout";
    clearConnection();

    //********************
    specificCleanUpOnTimeout();
    //********************
    return true;
}

void akg::NbJob::clearConnection() noexcept
{
    if (fdRef.isOpen() && selectorPtr)
    {
        selectorPtr->clearRead(fdRef());
        selectorPtr->clearWrite(fdRef());
        fdRef.close();
    }
}

void akg::NbJob::action() noexcept
{
    lastActionTime = currentTime;
}

int  akg::NbJob::getSocket() noexcept
{
    return fdRef();
}

void akg::NbJob::executeOnAccept() noexcept
{
}
bool akg::NbJob::setReading() noexcept
{
    if (selectorPtr == NULL)
    {
        return false;
    }
    selectorPtr->setRead(fdRef());
    status = wks_reading;
    return true;
}

bool akg::NbJob::setWriting() noexcept
{
    if (selectorPtr == NULL)
    {
        return false;
    }
    selectorPtr->setWrite(fdRef());
    status = wks_writing;
    return true;
}

int akg::NbJob::getErrno() noexcept
{
    return fdRef.getErrno();
}

//##################################################################
akg::NbServerJob::NbServerJob() noexcept
    : NbJob(serverSocket)
{
}

void akg::NbServerJob::initOnAttach(Selector* pSelector) noexcept
{
    selectorPtr = pSelector;
}


akg::NbJob::acceptStatus akg::NbServerJob::acceptConnection(ListenSocket& listenSocket) noexcept
{
    LDEBUG << "Am intrat in accepting";
    assert(currentBufferPtr != NULL);

    if (status != wks_accepting)
    {
        return acs_Iambusy;
    }
    action();

    if (serverSocket.acceptFrom(listenSocket) == false)
    {
        int saveerrno = serverSocket.getErrno();
        if (saveerrno == EAGAIN)
        {
            LDEBUG << "No pending connections";
        }
        else
        {
            LDEBUG << "Accept error " << saveerrno;
        }
        return acs_nopending;
    }

    serverSocket.setNonBlocking(true);

    setReading();

    executeOnAccept();
    LDEBUG << "Accept: Socket=" << fdRef();
    return acs_accepted;
}


akg::SocketAddress akg::NbServerJob::getClientSocketAddress() noexcept
{
    return serverSocket.getPeerAddress();
}

akg::HostAddress akg::NbServerJob::getClientHostAddress() noexcept
{
    return serverSocket.getPeerAddress().getHostAddress();
}

void akg::NbServerJob::readyToWriteAnswer() noexcept
{
    currentBufferPtr->clearToWrite();

    selectorPtr->clearRead(serverSocket());
    selectorPtr->setWrite(serverSocket());
    action();

    status = wks_writing;
}
//##################################################################

akg::NbClientJob::NbClientJob() noexcept
    : NbJob(clientSocket)
{
}

bool akg::NbClientJob::connectToServer(const char* serverHost, int serverPort) noexcept
{
    if (clientSocket.open(serverHost, serverPort))
    {
        clientSocket.setNonBlocking(true);
        selectorPtr->setWrite(clientSocket());
        status = wks_writing;
        action();
        return true;
    }
    return false;
}

void akg::NbClientJob::initOnAttach(Selector* pselector) noexcept
{
    selectorPtr = pselector;

    if (status == wks_writing)
    {
        selectorPtr->setWrite(clientSocket());
    }
}

akg::NbJob::acceptStatus
akg::NbClientJob::acceptConnection(ListenSocket&) noexcept
{
    // we don't accept connections
    return acs_Iambusy;
}

void akg::NbClientJob::readyToReadAnswer() noexcept
{
    currentBufferPtr->clearToRead();

    selectorPtr->clearWrite(clientSocket());
    selectorPtr->setRead(clientSocket());
    action();

    status = wks_reading;
}

//##################################################################
akg::NbCommunicator::NbCommunicator() noexcept
{
    maxJobs = 0;
    jobPtr = NULL;
}
akg::NbCommunicator::NbCommunicator(int newMaxJobs)
{
    jobPtr = NULL;
    initJobs(newMaxJobs);
}

bool akg::NbCommunicator::initJobs(int newMaxJobs)
{
    if (jobPtr != NULL)
    {
        return false;
    }
    maxJobs = newMaxJobs;
    jobPtr  = new JobPtr[maxJobs];

    for (int i = 0; i < maxJobs; i++)
    {
        jobPtr[i] = 0;
    }
    return true;
}

akg::NbCommunicator::~NbCommunicator() noexcept
{
    if (jobPtr != NULL)
    {
        delete[] jobPtr;
    }
}

bool akg::NbCommunicator::attachJob(NbJob& newJob) noexcept
{
    int freeSlot  = -1;
    for (int i = 0; i < maxJobs; i++)
    {
        if (jobPtr[i] == &newJob)
        {
            return false;    // job e in lista
        }
        if (jobPtr[i] == NULL && freeSlot == -1)
        {
            freeSlot = i;
        }
    }
    if (freeSlot == -1)
    {
        return false;
    }

    jobPtr[freeSlot] = &newJob;
    newJob.initOnAttach(&selector);
    return true;
}

bool akg::NbCommunicator::deattachJob(NbJob& oldJob) noexcept
{
    for (int i = 0; i < maxJobs; i++)
    {
        if (jobPtr[i] == &oldJob)
        {
            jobPtr[i] = NULL;
            oldJob.clearConnection();
            oldJob.initOnAttach(NULL);
            return true;
        }
    }
    return false;
}

bool akg::NbCommunicator::mayExit() noexcept
{
    if (exitRequest == false)
    {
        return false;
    }

    closeSocket(listenSocket); // we don't accept requests any more

    for (int i = 0; i < maxJobs; i++)
    {
        if (jobPtr[i] == NULL)
        {
            continue;
        }
        if (jobPtr[i]->isOperationPending())
        {
            return false;    // no, we have pending
        }
    }
    return true; // ok, we may exit
}

bool akg::NbCommunicator::runServer() noexcept
{
    if (listenPort == 0)
    {
        return false;
    }

    if (initListenSocket(listenPort, true) == false)
    {
        return false;
    }

    return mainLoop();
}

bool akg::NbCommunicator::runClient() noexcept
{
    return mainLoop();
}

bool akg::NbCommunicator::mainLoop() noexcept
{
    exitRequest = false;

    while (mayExit() == false)
    {
        LDEBUG << "Waiting for calls";

        if (executeBeforeSelect() == false)
        {
            return false;
        }

        int rasp = selector();

        akg::NbJob::setCurrentTime();

        if (executeAfterSelect() == false)
        {
            return false;
        }

        if (rasp > 0)
        {
            LDEBUG << "Ringing";
            // first this, to increase the chance to free a client
            dispatchWriteRequest();
            connectNewClients();
            dispatchReadRequest();
            processJobs();
            lookForTimeout(); // important!
        }
        if (rasp == 0)
        {
            LDEBUG << "Timeout";
            lookForTimeout();
            if (executeOnTimeout() == false)
            {
                return false;
            }
        }
        if (rasp < 0)
        {
            LDEBUG << "select error: " << strerror(errno);
        }
    }
    return true;
}

void akg::NbCommunicator::processJobs() noexcept
{
    LDEBUG << "process Jobs - entering";

    for (int i = 0; i < maxJobs; i++)
    {
        if (jobPtr[i] == NULL)
        {
            continue;
        }

        JobPtr& currentJob = jobPtr[i];

        if (currentJob->isProcessing())
        {
            LDEBUG << "job " << i << " is processing";

            currentJob->processRequest();
        }
    }
}

void akg::NbCommunicator::lookForTimeout() noexcept
{
    LDEBUG << "Looking for timeout";

    for (int i = 0; i < maxJobs; i++)
    {
        if (jobPtr[i] == NULL)
        {
            continue;
        }

        jobPtr[i]->cleanUpIfTimeout();
    }
}

void akg::NbCommunicator::dispatchWriteRequest() noexcept
{
    LDEBUG << "Dispatch writing";
    int i;
    for (i = 0; i < maxJobs; i++)
    {
        if (jobPtr[i] == NULL)
        {
            continue;
        }

        JobPtr& currentJob = jobPtr[i];

        if (currentJob->isWriting())
        {
            LDEBUG << "job " << i << ' ' << currentJob->getSocket() << " is active";
            if (selector.isWrite(currentJob->getSocket()))
            {
                LDEBUG << "...and may write ";
                currentJob->writePartialMessage();
            }
        }
    }
}

void akg::NbCommunicator::dispatchReadRequest() noexcept
{
    LDEBUG << "Dispatch reading";
    int i;
    for (i = 0; i < maxJobs; i++)
    {
        if (jobPtr[i] == NULL)
        {
            continue;
        }

        JobPtr& currentJob = jobPtr[i];

        if (currentJob->isReading())
        {
            LDEBUG << "job " << i << ' ' << currentJob->getSocket() << " is active";
            if (selector.isRead(currentJob->getSocket()))
            {
                LDEBUG << "... and has message";
                currentJob->readPartialMessage();
            }
        }
    }
}

void akg::NbCommunicator::connectNewClients() noexcept
{
    LDEBUG << "connect listenSocket=" << listenSocket();

    if (selector.isRead(listenSocket()) == false)
    {
        return;
    }

    LDEBUG << "Client is calling";

    akg::NbJob::acceptStatus status;

    for (int i = 0; i < maxJobs; i++)
    {
        if (jobPtr[i] == NULL)
        {
            continue;
        }

        JobPtr& currentJob = jobPtr[i];

        if (currentJob->isAccepting())
        {
            // we try to connect as much pending connections as possible
            status = currentJob->acceptConnection(listenSocket);

            if (status == akg::NbJob::acs_nopending)
            {
                break;
            }
            // there is no pending request,
            LDEBUG << "Connected client " << i << " on socket " << currentJob->getSocket();
        }
    }
}

bool akg::NbCommunicator::executeBeforeSelect() noexcept
{
    // false means server exit immediately
    return true;
}

bool akg::NbCommunicator::executeAfterSelect() noexcept
{
    // false means server exit immediately
    return true;
}

bool akg::NbCommunicator::executeOnTimeout() noexcept
{
    // false means server exit immediately
    return true;
}

