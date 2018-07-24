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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.restlet.resource.ClientResource;
import org.restlet.resource.Get;
import org.restlet.resource.Put;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Local representative of a remote node in Roboception's REST-API.
 *
 * Example: It could represent the rc_stereocamera node on Roboception's
 * rc_visard with which the camera parameters can be configured.
 *
 *
 * It allows to query the node's current status and gives access to all of its
 * parameters and services.
 *
 * @author emmerich
 *
 */
public class Node extends GenericPrintable
{

    /**
     * Basic informations about a node in Roboception's REST-API
     *
     */
    public static class Info extends GenericPrintable
    {
        public String name;
        public Node.Status.ProcessingStatus status;
        public ArrayList<String> parameters, services;
    }

    /**
     * Class for wrapping a set of parameters of an rcapi node.
     *
     * Allows syncing to and from a remote node's parameters all in once or via
     * individual access.
     *
     * @author emmerich
     *
     */
    public static class Parameters
    {

        /**
         * Create a local representative of all parameters of a remote rcapi
         * node.
         *
         * @param remoteHost
         * @param node
         * @return
         */
        public static Parameters connectTo(final String remoteHost,
                final String node)
        {
            return new Parameters(remoteHost, node);
        }

        /**
         * Synchronize all parameters' values to the remote representatives.
         *
         * This sets all remote node parameters' values to the local values.
         *
         * Note: This is an in-place operation (just for convenience). The
         * returned Parameters object refers to this.
         *
         * Note: This call might also change the local values in case they are
         * not accepted by the remote resource.
         *
         * @return reference to this Parameters object
         */
        public Parameters syncToRemote()
        {
            this.setParamValuesFrom(remote.put(paramList));
            return this;
        }

        /**
         * Overwrite the local parameters with values from the remote
         * representatives.
         *
         * Note: This is an in-place operation. The return value refers to this
         * changed Parameters object, and is just for convenience.
         *
         * @return reference to this Parameters object
         */
        public Parameters syncFromRemote()
        {
            this.setParamValuesFrom(remote.get());
            return this;
        }

        /**
         * Get read, write and sync access to a single Parameter of this node.
         *
         * @param name
         *            of the Parameter
         * @return reference to the single Parameter
         */
        public Parameter get(String name)
        {
            if (!paramMap.containsKey(name))
            {
                throw new IllegalArgumentException("Parameter '" + name
                        + "' does not exist!\nAvailabe parameters: "
                        + paramMap.keySet());
            }
            return paramMap.get(name);
        }

        @Override
        public String toString()
        {
            return paramList.toString();
        }

        protected Parameters(final String remoteHost, final String node)
        {
            ClientResource resource = new RCClientResource(ApiUrls.parameters(
                    remoteHost, node));
            resource.setRequestEntityBuffering(true);
            resource.setResponseEntityBuffering(true);
            remote = resource.wrap(ClientInterface.class);

            // initial full sync from remote, creating hash map as well
            paramList = remote.get();
            paramMap = new HashMap<String, Parameter>();
            for (Parameter param : paramList)
            {
                param.setRemote(remoteHost, node);
                paramMap.put(param.getName(), param);
            }
        }

        /**
         * Updates this Parameters values from others
         *
         * @param others
         *            list of Parameter
         */
        protected void setParamValuesFrom(List<Parameter> others)
        {
            for (Parameter otherParam : others)
            {
                Parameter localParam = paramMap.get(otherParam.name);
                if (localParam != null)
                {
                    localParam.setFromOther(otherParam);
                }
            }
        }

        @SuppressWarnings("serial")
        protected static class ParamListType extends ArrayList<Parameter>
        {
        };

        protected static interface ClientInterface
        {
            @Get
            public ParamListType get();

            @Put
            public ParamListType put(ParamListType l);

        }

        // / remote resource of this Parameter
        protected ClientInterface remote;

        // / this node's parameters
        protected final ParamListType paramList;
        protected final Map<String, Parameter> paramMap;

    }

    /**
     * Representation of an rcapi node's current status.
     *
     * @author emmerich
     *
     */
    public static class Status extends GenericPrintable
    {

        public enum ProcessingStatus
        {
            running, stale, down, unknown
        }

        // / the node's processing status
        public ProcessingStatus processingStatus;

        // / the UNIX timestamp of the last updated status
        public double timestamp;

        // / the node-specific status values
        public Map<String, String> values;

        public Status syncFromRemote()
        {
            setFromJson(resource.get(JsonNode.class));
            return this;
        }

        protected void setFromJson(final JsonNode json)
        {
            processingStatus = ProcessingStatus.valueOf(json.get("status")
                    .asText());
            timestamp = json.get("timestamp").asDouble();
            values = new HashMap<String, String>();
            if (json.has("values"))
            {
                Iterator<Entry<String, JsonNode>> jsonValues = json.get(
                        "values").fields();
                while (jsonValues.hasNext())
                {
                    Entry<String, JsonNode> jsonValue = jsonValues.next();
                    values.put(jsonValue.getKey(), jsonValue.getValue()
                            .asText());
                }
            }
        }

        protected Status(final String remoteHost, final String node)
        {
            try
            {
                ignoreFieldWhenPrinting(getClass().getDeclaredField("resource"));
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
            resource = new RCClientResource(ApiUrls.statusURL(remoteHost, node));
            syncFromRemote();
        }

        protected final ClientResource resource;
    }

    public static Node connectTo(final String remoteHost, final String node)
    {
        return new Node(remoteHost, node);
    }

    public Info getInfo() {
        return info;
    }

    /**
     * Access to all params of this node, e.g. to sync all at once or to print
     * them
     *
     * @return all params of this node as Node.Parameters object
     */
    public Parameters getParameters()
    {
        return params;
    }

    /**
     * Get list of available parameters
     *
     * @return
     */
    public List<String> getAvailableParameters()
    {
        return new ArrayList<String>(params.paramMap.keySet());
    }

    /**
     * Access to a single Parameter object
     *
     * @param name
     * @return
     */
    public Parameter getParameter(final String name)
    {
        return params.get(name);
    }

    /**
     * Get access to the node status.
     *
     * Note: In order to query the current status from remote, you need to
     * syncFromRemote() the status.
     *
     * @return
     */
    public Status getStatus()
    {
        return status;
    }

    public Service getService(final String name)
    {
        if (!services.containsKey(name))
        {
            throw new IllegalArgumentException("Service '" + name
                    + "' does not exist!\nAvailabe services: "
                    + services.keySet());
        }
        return services.get(name);
    }

    public List<String> getAvailableServices()
    {
        return new ArrayList<String>(services.keySet());
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(info.name);

        // Node status
        sb.append("\nstatus: " + status);

        // Node params
        {
            sb.append("\nparameters: {");
            Iterator<Parameter> paramIt = params.paramList.iterator();
            while (paramIt.hasNext())
            {
                Parameter param = paramIt.next();
                sb.append(param.getName() + "=" + param.getValue()
                        + (paramIt.hasNext() ? "," : ""));
            }
            sb.append("}");
        }

        // Node services
        {
            sb.append("\nservices: {");
            Iterator<String> servIt = services.keySet().iterator();
            while (servIt.hasNext())
            {
                String serviceName = servIt.next();
                sb.append(serviceName + (servIt.hasNext() ? "," : ""));
            }
            sb.append("}");
        }

        return sb.toString();
    }

    /**
     * Connects to the node running on the remoteHost and initializes a local
     * representation of it - with status, parameters, and service definitions
     *
     * @param remoteHost
     * @param node
     */
    protected Node(final String remoteHost, final String node)
    {

        // gather initial info about this node
        ClientResource tmpInfoResource = new RCClientResource(ApiUrls.node(remoteHost, node));
        info = tmpInfoResource.get(Info.class);

        // initial creation of params - syncing from remote of all param data
        params = Parameters.connectTo(remoteHost, node);

        // initial creation of node status - include syncing from remote
        status = new Status(remoteHost, node);

        // creation of services - one sync from remote for getting all
        // descriptions
        services = new HashMap<String, Service>();
        ClientResource tmpServicesGetter = new RCClientResource(
                ApiUrls.services(remoteHost, node));
        _ServiceInfoList serviceInfos = tmpServicesGetter
                .get(_ServiceInfoList.class);
        for (Service.Info serviceInfo : serviceInfos)
        {
            Service s = new Service();
            s.info = serviceInfo;
            s.setRemote(remoteHost, node, serviceInfo.name, false);
            services.put(serviceInfo.name, s);
        }

    }

    @SuppressWarnings("serial")
    protected static class _ServiceInfoList extends
            ArrayList<Service.Info>
    {
    };

    protected final Parameters params;
    protected final Map<String, Service> services;
    protected final Status status;
    protected final Info info;
}
