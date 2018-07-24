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
import java.util.List;
import java.util.Map;

import org.restlet.resource.ClientResource;
import org.restlet.resource.Get;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Representation of an rc_visard's remote REST-API providing
 *
 * * access to all its nodes with their states, parameters, and services *
 * access to current system state
 *
 * @author emmerich
 *
 */
public class Visard
{

    /**
     * Client to gather the full system state of Roboception's rc_visard.
     *
     * @author emmerich
     *
     */
    public static class SysInfo extends GenericPrintable
    {
        public static class FirmwareInfo extends GenericPrintable
        {

            public static class ImageInfo extends GenericPrintable
            {
                public String image_version;
            }

            public ImageInfo active_image, inactive_image;
            public String next_boot_image;
            public boolean fallback_booted;
        }

        public static class NtpStatus extends GenericPrintable
        {
            public String accuracy;
            @JsonProperty("synchronized")
            public boolean is_synchronized;
        }

        public static class PtpStatus extends GenericPrintable
        {
            public enum State
            {
                off, unknown, INITIALIZING, FAULTY, DISABLED, LISTENING, PASSIVE, UNCALIBRATED, SLAVE
            }

            public State state;
            public String master_ip;
            public double offset, offset_dev, offset_mean;
        }

        public FirmwareInfo firmware;
        public NtpStatus ntp_status;
        public PtpStatus ptp_status;
        public String hostname, mac, serial;
        public int link_speed;
        public double time, uptime;
        public boolean ready;

        public SysInfo syncFromRemote()
        {
            SysInfo got = remote.get();
            this.firmware = got.firmware;
            this.hostname = got.hostname;
            this.link_speed = got.link_speed;
            this.mac = got.mac;
            this.ntp_status = got.ntp_status;
            this.ptp_status = got.ptp_status;
            this.ready = got.ready;
            this.serial = got.serial;
            this.time = got.time;
            this.uptime = got.uptime;
            return this;
        }

        /**
         * client interface for retrieving the SysInfo from the remote resource
         */
        protected static interface ClientInterface
        {
            @Get
            public SysInfo get();
        }

        // / remote resource of this SysInfo
        @JsonIgnore
        protected ClientInterface remote;

        protected void setRemote(final String host)
        {
            ClientResource resource = new RCClientResource(
                    ApiUrls.entrypoint(host) + "/system");
            resource.setRequestEntityBuffering(true);
            resource.setResponseEntityBuffering(true);
            remote = resource.wrap(ClientInterface.class);
        }

        // / prevent public instantiation
        protected SysInfo()
        {
        }
    }

    public static Visard connectTo(final String remoteHost)
    {
        return new Visard(remoteHost);
    }

    public Node getNode(final String name)
    {
        if (!nodes.containsKey(name))
        {
            throw new IllegalArgumentException("Node '" + name
                    + "' does not exist!\nAvailabe nodes: " + nodes.keySet());
        }
        Node node = nodes.get(name);

        // create it if it does not exist yet, i.e. map contains only a place
        // holder
        if (node == null)
        {
            node = Node.connectTo(host, name);
            nodes.put(name, node);
        }

        // else access it
        return node;
    }

    /**
     * Gets the current system info from the remote
     *
     * @return
     */
    public SysInfo getSystemInfo()
    {
        return systemInfo;
    }

    public List<Node.Info> getAvailableNodes()
    {
        return nodeInfos;
    }

    @SuppressWarnings("serial")
    public static class NodeInfoList extends ArrayList<Node.Info>
    {
    };

    protected Visard(final String remoteHost)
    {
        host = remoteHost;

        // gather infos about all nodes
        ClientResource nodesResource = new RCClientResource(
                ApiUrls.nodes(remoteHost));
        nodesResource.setRequestEntityBuffering(true);
        nodesResource.setResponseEntityBuffering(true);
        nodeInfos = nodesResource.get(NodeInfoList.class);

        // initialize node-map with node skeletons
        nodes = new HashMap<String, Node>();
        for (Node.Info nodeInfo : nodeInfos)
        {
            nodes.put(nodeInfo.name, null);
        }

        // connect SystemInfo client and do initial sync
        systemInfo = new SysInfo();
        systemInfo.setRemote(host);
        systemInfo.syncFromRemote();
    }

    protected final String host;
    protected final Map<String, Node> nodes;
    protected final SysInfo systemInfo;
    protected final List<Node.Info> nodeInfos;
}
