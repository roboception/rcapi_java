/*
 * Copyright (c) 2018 Roboception GmbH
 * All rights reserved
 *
 * Author: Christian Emmerich
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.roboception.rcapi.core;

import java.util.ArrayList;

import org.restlet.resource.ClientResource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Represents a single service offered by a node in Roboception's REST-API
 *
 * @author emmerich
 */
public class Service extends GenericPrintable
{
    /**
     * Some information about the offered service
     *
     * @author emmerich
     *
     */
    public static class Info extends GenericPrintable
    {
        public String name, description;
        public JsonNode args, response;
    }

    /**
     * Creates and binds a service client to a specific device, node, and
     * service.
     *
     * @param host
     *            - the device's host name (DNS) or IP address as known in the
     *            network
     * @param node
     *            - the name of the node that offers the service
     * @param service
     *            - the name of the offered service
     * @return a ServiceClient object
     */
    public static Service connectTo(String host, String node,
            String service)
    {
        Service s = new Service();
        s.setRemote(host, node, service, true);
        return s;
    }

    /**
     * Defines the java class as which the service response shall be returned.
     *
     * Default: com.fasterxml.jackson.databind.JsonNode
     *
     * @param clazz
     */
    public void setResponseType(Class<?> clazz)
    {
        userDefinedResponseType = clazz;
    }

    /**
     * Returns description and other information about this offered service
     *
     * @return ServiceInfo
     */
    public Info getInfo()
    {
        return info;
    }

    /**
     * Call a node's service without any arguments.
     *
     * @return the result of the ServiceClient's call
     */
    public Object call()
    {
        return call(null);
    }

    /**
     * Call a node's service with arguments
     *
     * @param serviceArgs
     *            the service call's arguments
     * @return the result of the ServiceClient's call
     */
    public Object call(final Object serviceArgs)
    {

        // / create request object and fill with service args if not null or
        // empty object, with empty list otherwise
        Object req;
        if (serviceArgs != null
                && serviceArgs.getClass().getDeclaredFields().length > 0)
        {
            req = new Object()
            {
                @SuppressWarnings("unused")
                public Object args = serviceArgs;
            };
        } else
        {
            req = new Object()
            {
                @SuppressWarnings("unused")
                public Object args = new ArrayList<Object>();
            };
        }

        // / if return type is void, we just issue the call and return null
        if (userDefinedResponseType.equals(Void.class)
                || userDefinedResponseType.equals(Void.TYPE))
        {
            resource.put(req);
            return null;
        }

        // / else we get result as JsonNode, extract the actual 'response', and
        // / return the 'response' JsonNode as a Java object
        JsonNode json = resource.put(req, JsonNode.class);
        if (!json.has("response"))
        {
            throw new RuntimeException(
                    "Service returned JSON object in wrong format!"
                            + "\nExpected a field 'response' but got: " + json);
        }
        json = json.get("response");

        try
        {
            return mapper.readValue(json.traverse(),
                    userDefinedResponseType);
        }
        // TODO: more appropriate exception handling. When user sets
        // a wrong response Type (see setResponseType) a JsonMappingException
        // is issued
        catch (Exception e)
        {
            throw new RuntimeException(
                    "Caught exception while trying to parse the service call's response!",
                    e);
        }
    }

    // / Service description
    protected Info info;

    // / This represents a resource to do requests on
    protected ClientResource resource;

    // / mapper for converting Json into Java class
    protected ObjectMapper mapper = new ObjectMapper();
    protected Class<?> userDefinedResponseType;

    protected Service()
    {
        info = null;
        resource = null;
        userDefinedResponseType = JsonNode.class;

        // ignore some of this classes fields for generic printing
        try
        {
            ignoreFieldWhenPrinting(getClass().getDeclaredField("resource"));
            ignoreFieldWhenPrinting(getClass().getDeclaredField("mapper"));
        } catch (Exception e)
        {
            throw new RuntimeException(
                    "This should never happen! Caught exception during initialization of ServiceClient: "
                            + e.getLocalizedMessage());
        }
    }

    protected void setRemote(String host, String node, String service,
            boolean initialSyncFromRemote)
    {

        resource = new RCClientResource(ApiUrls.service(host, node, service));
        resource.setRequestEntityBuffering(true);
        resource.setResponseEntityBuffering(true);

        if (initialSyncFromRemote)
        {
            // initially connect to service and get all service infos
            info = resource.get(Info.class);
        }
    }
}