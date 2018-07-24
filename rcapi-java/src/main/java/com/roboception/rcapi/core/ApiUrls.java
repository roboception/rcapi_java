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

/**
 * Convenience methods to create full URLs matching Roboception's REST-API
 * structure.
 *
 * @author emmerich
 *
 */
public final class ApiUrls
{

    /// base entrypoint of the API
    public static String entrypoint(final String host)
    {
        return "http://" + host + "/api/v1";
    }

    /// nodes' URI
    public static String nodes(final String host)
    {
        return entrypoint(host) + "/nodes";
    }

    /// a single node
    public static String node(final String host, final String node)
    {
        return nodes(host) + "/" + node;
    }

    /// services' URI
    public static String services(String host, String node)
    {
        return node(host, node) + "/services";
    }

    /// a single service
    public static String service(String host, String node, String service)
    {
        return services(host, node) + "/" + service;
    }

    /// parameters of a single node
    public static String parameters(String host, String node)
    {
        return node(host, node) + "/parameters";
    }

    /// a node's single parameter
    public static String parameter(String host, String node, String param)
    {
        return parameters(host, node) + "/" + param;
    }

    /// a node's status
    public static String statusURL(String host, String node)
    {
        return node(host, node) + "/status";
    }

    /// dynamics' data streams
    public static String streamsURL(String host)
    {
        return entrypoint(host) + "/datastreams";
    }

    /// a single data stream's URI
    public static String streamURL(String host, String streamName)
    {
        return streamsURL(host) + "/" + streamName;
    }

    private ApiUrls()
    {
        // to prevent instantiation
    }

}
