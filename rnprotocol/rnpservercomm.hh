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
/*************************************************************
 *
 *
 * PURPOSE:
 *
 *
 * COMMENTS:
 *
 ************************************************************/

#ifndef RNPSERVERCOMM_HH
#define RNPSERVERCOMM_HH

//#include <rnprotocol.hh>
//#include <rnpembedded.hh>
#include "rnpcommunication.hh"
#include "raslib/error.hh"

using namespace rnp;

//function prototypes:
size_t
my_strftime(char* s, size_t max, const char* fmt, const struct tm* tm);

const char*
now();

/**
  * \ingroup Rnprotocols
  */
class ClientTimer
{
public:
    ClientTimer();
    void setTimeoutInterval(int seconds);
    void markAction();
    bool checkForTimeout();
private:
    time_t interval;
    time_t lastAction;
    bool   enabled;
};

/**
  * \ingroup Rnprotocols
  */
class RnpRasserverJob : public RnpServerJob
{
public:
    RnpRasserverJob() noexcept;

private:
    bool validateMessage() noexcept;
    void executeOnAccept() noexcept;
    void executeOnWriteReady() noexcept;
    void specificCleanUpOnTimeout() noexcept;
    void executeOnReadError() noexcept;
    void executeOnWriteError() noexcept;
};


/**
  * \ingroup Rnprotocols
  */
class RnpRasDaManComm : public RnpBaseServerComm
{
public:
    RnpRasDaManComm() noexcept;

    ~RnpRasDaManComm() noexcept;

    void processRequest(CommBuffer* receiverBuffer, CommBuffer* transmiterBuffer, RnpTransport::CarrierProtocol, RnpServerJob* callingJob) noexcept;

    void setTimeoutInterval(int seconds);
    void checkForTimeout();

private: // inherited from RnpBaseServerComm
    RnpServerJob* createJobs(int howMany);

    void decodeFragment();

    ClientTimer  clientTimer;
private: // the execution functions:
    void executeConnect();
    void executeDisconnect();
    void executeOpenDB();
    void executeCloseDB();
    void executeBeginTA();
    void executeCommitTA();
    void executeAbortTA();
    void executeIsTAOpen();
    void executeQueryHttp();
    void executeGetNewOId();
    void executeQueryRpc();
    void executeGetNextElement();
    void executeEndTransfer();
    void executeGetNextMDD();
    void executeGetNextTile();

    void executeUpdateQuery();
    void executeInsertQuery();
    void executeStartInsertTransMDD();
    void executeInsertTile();
    void executeEndInsertMDD();
    void executeInitUpdate();
    void executeGetTypeStructure();
    void executeStartInsertPersMDD();
    void executeInsertMDD();
    void executeInsertCollection();
    void executeRemoveObjFromColl();
    void executeDeleteObjByOId();
    void executeDeleteCollByName();
    void executeGetCollection();
    void executeGetCollectionOIds();
    void executeGetObjectType();
    void executeSetFormat();


    void executeCreateCollection();
    void executeCreateMDD();
    void executeExtendMDD();
    void executeGetTileDomains();

    void answerr_Error(r_Error&);
private: // helper functions
    void connectClient();
    // reset connection, without reporting availability to rasmgr
    void disconnectInternally();
    // reset connection, with reporting availability to rasmgr
    void disconnectClient();
    void verifyClientID(RnpQuark command);
    int  makeNewClientID();

    int  clientID;         // un timestamp, de fapt!
    int  requestCounter;   // numara pachetele trimise de un client
    int  fragmentCounter;  // numara fragmentele trimise de un client

    static const int NoClient;
};


/**
  * \ingroup Rnprotocols
  */
class RasserverCommunicator : public NbCommunicator
{
public:
    RasserverCommunicator(RnpRasDaManComm*) noexcept;

protected:
    bool executeOnTimeout() noexcept;

    RnpRasDaManComm* commPtr;
};

#endif // RNPSERVERCOMM_HH

