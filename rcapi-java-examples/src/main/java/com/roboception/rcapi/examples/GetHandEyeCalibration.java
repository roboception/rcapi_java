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

import com.roboception.rcapi.core.GenericPrintable;
import com.roboception.rcapi.core.Service;

public class GetHandEyeCalibration
{

    public static void printUsage()
    {
        System.out
                .println("Connects to the rc_hand_eye_calibration node on an rc_visard\n"
                        + "and querries the latest stored hand-eye calibration transform\n"
                        + "via service call.\n\n"
                        + "Usage:\t GetHandEyeCalibration.java <rc-visard-hostname-or-ip>\n\n"
                        + "Examples:"
                        + "\n\t GetHandEyeCalibration.java rc-visard-02938425.local"
                        + "\n\t GetHandEyeCalibration.java 10.0.2.55\n");
    }

    /**
     * Java-Bean representation of the service call's response. See
     * service.setResponseType(CalibrationResponse.class)
     *
     * @author emmerich
     *
     */
    public static class CalibrationResponse extends GenericPrintable
    {
        public static class Pose extends GenericPrintable
        {
            public static class Position extends GenericPrintable
            {
                public double x, y, z;
            }

            public static class Orientation extends GenericPrintable
            {
                public double x, y, z, w;
            }

            public Position position = new Position();
            public Orientation orientation = new Orientation();
        }

        public Pose pose = new Pose();
        public boolean robot_mounted;
        public double error;

        public boolean success;
        public int status;
        public String message = new String();
    }

    public static void main(String[] args)
    {
        if (args.length != 1)
        {
            System.out
                    .println("ERROR: Wrong number of command line arguments!");
            printUsage();
            return;
        }

        // connect to service and print some infos about it
        String host = args[0];
        System.out
                .println("\nTrying to get hand-eye-calibration data from rc_visard...");
        Service service = Service.connectTo(host, "rc_hand_eye_calibration",
                "get_calibration");

        // each service contains a description...
        System.out.println("Connected to service:");
        Service.Info info = service.getInfo();
        System.out.println("  " + info.name + " - " + info.description);
        // ... and information about what data is required as arguments...
        System.out.println("  required arguments: " + info.args);
        // ... and what data is returned in the response
        System.out.println("  returning result: " + info.response);

        // if we can we should define the Java class as which the response
        // should be returned - otherwise results would be returned as Json
        // objects
        service.setResponseType(CalibrationResponse.class);

        // now call the hand-eye calibration service
        System.out.println("\nGetting hand-eye calibration transform...");
        Object result = service.call();

        // if we defined proper return type we can simply cast the result
        CalibrationResponse calibResponse = (CalibrationResponse) result;

        // otherwise results would be returned as Json objects:
        //    JsonNode json = (JsonNode) service.call();

        // check and print the result
        System.out.println("Service call returned object of type "
                + calibResponse.getClass().getName());
        if (calibResponse.success)
        {
            System.out.println("Returned calibration: " + calibResponse.pose);
        } else
        {
            System.out
                    .println("Error: Service call did not return calibration. Returned reason: "
                            + calibResponse.message);
        }

    }
}
