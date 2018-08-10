#include "clientquerystreamedresult.hh"

namespace rasserver
{

const boost::uint64_t ClientQueryStreamedResult::CHUNK_SIZE;

ClientQueryStreamedResult::ClientQueryStreamedResult(char* dataArg, boost::uint64_t lengthArg, const std::string& clientUUIDArg)
    : data(dataArg), length(lengthArg), clientUUID(clientUUIDArg), offset(0)
{

}

DataChunk ClientQueryStreamedResult::getNextChunk()
{
    DataChunk chunk;

    chunk.length = this->getRemainingBytesLength() < CHUNK_SIZE ? this->getRemainingBytesLength() : CHUNK_SIZE;
    chunk.bytes = this->data + this->offset;

    this->offset += chunk.length;

    return chunk;
}

std::string ClientQueryStreamedResult::getClientUUID() const
{
    return this->clientUUID;
}

boost::uint64_t ClientQueryStreamedResult::getRemainingBytesLength() const
{
    return this->length - this->offset;
}

ClientQueryStreamedResult::~ClientQueryStreamedResult()
{
    delete[] this->data;
    this->data = NULL;
}

}
