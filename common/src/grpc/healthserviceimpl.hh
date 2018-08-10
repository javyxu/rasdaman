#ifndef COMMON_SRC_GRPC_HEALTHSERVICEIMPL_HH
#define COMMON_SRC_GRPC_HEALTHSERVICEIMPL_HH

#include <boost/smart_ptr.hpp>
#include <boost/thread.hpp>
#include "common/src/grpc/messages/health_service.grpc.pb.h"

namespace common
{
/**
 * @brief The HealthServiceImpl class
 * Health checks are used to probe whether the server is able to handle rpcs.
 * A server may choose to reply “unhealthy” because it is not ready to take requests or for some other reason.
 * The client can act accordingly if the response is not received within some time window or the response says unhealthy in it.
 * A client can query the server’s health status by calling the Check method, and a deadline should be set on the rpc.
 * The client can optionally set the service name it wants to query for health status.
 * The suggested format of service name is package_names.ServiceName
 */
class HealthServiceImpl: public HealthService::Service
{
public:
    HealthServiceImpl();

    virtual ~HealthServiceImpl();

    /**
     * @brief setStatus Set the health status of a GRPC service.
     * @param service String uniquely identifying the service.
     * @param status ServingStatus which can be any of UNKNOWN, SERVING, NOT_SERVING
     */
    virtual void setStatus(const std::string& service, const HealthCheckResponse::ServingStatus& status);

    /**
     * @brief clearStatus Clear the health status of the given GRPC service.
     * If a query will be received for the given service, NOT_SERVING will be returned.
     * @param service
     */
    virtual void clearStatus(const std::string& service);

    /**
     * @brief clearAll Clear all the status information.
     */
    virtual void clearAll();

    /**
     * @brief Check Will set the status field of the response to the status of the service
     * identified by the service field of the request.
     * If the service field is empty, SERVING will be returned representing the status of the server.
     * If the service status was set through the setStatus method, the set status will be returned.
     * If the service field is known by the HealthService, UNKNOWN will be returned.
     * @param context
     * @param request
     * @param response
     * @return
     */
    virtual grpc::Status Check(grpc::ServerContext* context, const HealthCheckRequest* request, HealthCheckResponse* response);

private:
    boost::mutex mutex;
    // Map with the meaning <serviceIdentifier, status>
    std::map<std::string, HealthCheckResponse::ServingStatus> statuses;
};
}

#endif // COMMON_SRC_GRPC_HEALTHSERVICEIMPL_HH
