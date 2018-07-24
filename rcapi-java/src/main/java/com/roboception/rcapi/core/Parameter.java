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

import org.restlet.resource.ClientResource;
import org.restlet.resource.Get;
import org.restlet.resource.Put;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.roboception.rcapi.core.GenericPrintable;

/**
 * A local {@link Parameter} entity that corresponds to a remote Parameter in
 * Roboception's REST-API.
 *
 * A {@link Parameter} is represented by a name, its type, and its value. It
 * further inhibits a description and information about min, max and default
 * values. This class offers methods to get and set this data as well as to
 * synchronize the local entity to/from the remote resource.
 *
 * @author emmerich
 */
public class Parameter extends GenericPrintable
{

    /**
     * Create a Parameter and connect it's inner client with a remote Parameter.
     * During creation an initial synchronization from the remote Parameter is
     * issued.
     *
     * @param host
     *            a server's host name (DNS) or IP address as known in the
     *            network
     * @param node
     *            the name of the node that exhibits the Parameter
     * @param name
     *            the name of the Parameter itself
     * @return
     */
    public static Parameter connectTo(final String host, final String node,
            final String name)
    {
        return (new Parameter(name, host, node)).syncFromRemote();
    }

    public String getName()
    {
        return name;
    };

    public String getDescription()
    {
        return description;
    };

    public String getType()
    {
        return type;
    };

    @SuppressWarnings("unchecked")
    public <T> T getValue()
    {
        return (T) value;
    };

    @SuppressWarnings("unchecked")
    public <T> T getMin()
    {
        return (T) min;
    };

    @SuppressWarnings("unchecked")
    public <T> T getMax()
    {
        return (T) max;
    };

    @SuppressWarnings("unchecked")
    public <T> T getDefault()
    {
        return (T) default_value;
    };

    public Parameter setValue(final Object value)
    {
        // TODO: check for type mismatch
        this.value = value;
        return this;
    };

    /**
     * Update the remote Parameter's value with the local value.
     *
     * Note: After syncing to remote, this local entity is updated with the
     * values from remote.
     */
    public Parameter syncToRemote()
    {
        this.setFromOther(remote.put(this));
        return this;
    }

    /**
     * Update local {@link Parameter} entity from remote resource
     */
    public Parameter syncFromRemote()
    {
        this.setFromOther(remote.get());
        return this;
    }

    /**
     * Set this Parameter's values from other Parameter
     *
     * @param other
     */
    protected void setFromOther(Parameter other)
    {
        name = other.name;
        description = other.description;
        type = other.type;
        value = other.value;
        min = other.min;
        max = other.max;
        default_value = other.default_value;
    }

    /**
     * Set remote representative of this parameter if not yet set.
     *
     * @param remoteHost
     * @param remoteNode
     */
    protected void setRemote(final String remoteHost, final String remoteNode)
    {
        ClientResource resource = new RCClientResource(ApiUrls.parameter(
                remoteHost, remoteNode, this.name));
        resource.setRequestEntityBuffering(true);
        resource.setResponseEntityBuffering(true);
        remote = resource.wrap(ClientInterface.class);
    }

    // / Properties of an rcapi Parameter
    protected String name, description, type;
    protected Object value, min, max;
    @JsonProperty("default")
    protected Object default_value;

    /**
     * client interface for sending and retrieving the Parameter to and from the
     * remote resource
     */
    protected static interface ClientInterface
    {
        @Get
        public Parameter get();

        @Put
        public Parameter put(Parameter p);
    }

    // / remote resource of this Parameter
    @JsonIgnore
    protected ClientInterface remote;

    /**
     * Constructor for Parameter with type and name
     *
     * @param type
     *            parameter type (String, Boolean, Integer, or Double)
     * @param name
     *            (remote) name of the Parameter
     */
    protected Parameter(final String name, final String host, final String node)
    {
        this();
        value = min = max = default_value = null;
        this.name = name;
        setRemote(host, node);
    }

    // / private default constructor for JSON
    private Parameter()
    {
    }
}
