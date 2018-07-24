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

package com.roboception.rcapi.examples;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

import com.roboception.rcapi.core.Node;
import com.roboception.rcapi.core.Visard;
import com.roboception.rcapi.core.Visard.SysInfo;

public class PrintRCVisardInfo
{

    public static void printUsage()
    {
        System.out
                .println("Connects to an rc_visard device and prints the current system state\n"
                        + "as well as available nodes, parameters, services.\n\n"
                        + "Usage:\t RCVisardInterfaceExample.java <rc-visard-hostname-or-ip>\n\n"
                        + "Examples:"
                        + "\n\t RCVisardInterfaceExample.java rc-visard-02938425.local"
                        + "\n\t RCVisardInterfaceExample.java 10.0.2.55\n");
    }

    public static void main(String[] args)
    {
        if (args.length != 1) {
            System.out.println("ERROR: Wrong number of command line arguments!");
            printUsage();
            return;
        }

        // connect to rc_visard
        String host = args[0];
        System.out.println("Connecting to " + host + "...");
        Visard rcvisard = Visard.connectTo(host);
        System.out.println("Successfully connected.");

        // print infos for all nodes available
        System.out.println("\nAvailable nodes, services, parameters:");
        System.out.println("---------------------------------------");
        List<Node.Info> nodeInfos = rcvisard.getAvailableNodes();
        for (Node.Info info : nodeInfos)
        {
            System.out.println("\n    " + info.name + ":");
            System.out.println("\t parameters:\t" + info.parameters);
            System.out.println("\t services:\t" + info.services);
            System.out.println("\t status:\t" + info.status);
        }

        // print system information
        System.out.println("\nSystem information:");
        System.out.println("--------------------\n");
        SysInfo sysInfo = rcvisard.getSystemInfo().syncFromRemote();
        System.out.println("    sys. ready:\t" + sysInfo.ready);
        System.out.println("    uptime:\t" + sysInfo.uptime + " secs");
        System.out.println("");
        System.out.println("    host name:\t" + sysInfo.hostname);
        System.out.println("    serial no.:\t" + sysInfo.serial);
        System.out.println("    mac addr.:\t" + sysInfo.mac);
        System.out.println("    firmware:\t"
                + sysInfo.firmware.active_image.image_version);
        System.out.println("");
        System.out.println("    time: "
                + (new SimpleDateFormat()).format(new Date(
                        (long) (sysInfo.time * 1000))));
        if (sysInfo.ntp_status.is_synchronized)
        {
            System.out.println("    time sync:\tntp");
            System.out.println("    accuracy:\t" + sysInfo.ntp_status.accuracy);
        } else if (!sysInfo.ptp_status.state
                .equals(SysInfo.PtpStatus.State.off))
        {
            System.out.println("    time sync:\tptp");
            System.out.println("    state:\t" + sysInfo.ptp_status.state);
            if (sysInfo.ptp_status.state.equals(SysInfo.PtpStatus.State.SLAVE))
            {
                System.out.println("    ptp master:\t"
                        + sysInfo.ptp_status.master_ip);
                System.out.println("    offset:\t" + sysInfo.ptp_status.offset);
                System.out.println("    avg. offset:\t"
                        + sysInfo.ptp_status.offset_mean);
                System.out.println("    stddev. offset:\t"
                        + sysInfo.ptp_status.offset_dev);
            }
        } else
        {
            System.out.println("    time sync:\toff");
        }
    }

}
