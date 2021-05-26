package org.epics.pvaccess.client.rpc.test;

import org.epics.pvaccess.client.rpc.RPCClientImpl;
import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.pv.FieldBuilder;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;

import java.util.HashMap;
import java.util.Map;

public class RPCServiceClientExample {
    private final static FieldCreate fieldCreate = FieldFactory.getFieldCreate();

    public static void main(String[] args) {
        try {
            if (args.length < 1) {
                throw new RuntimeException("PV not specified: usage\n" +
                        " RPCServiceClientExample service [arg=value[,arg=value]...]\n" +
                        "e.g. \n" +
                        " RPCServiceClientExample AIDA:SAMPLE:DEVICE1 arg1=value");
            }
            // Create a client to the service specified
            String pv = args[0];
            RPCClientImpl client = new RPCClientImpl(pv);

            // Create optional parameters
            Map<String, String> argMap = new HashMap<String, String>();
            FieldBuilder fieldBuilder = fieldCreate.createFieldBuilder();
            for (int i = 1; i < args.length; i++) {
                String arg = args[i];
                String[] keyValue = arg.split("=");

                // Add parameter
                fieldBuilder.add(keyValue[0], ScalarType.pvString);

                // Add arg value to values list - to be marshalled later
                argMap.put(keyValue[0], keyValue[1]);
            }

            // Set any parameter values if specified
            PVStructure arguments = PVDataFactory.getPVDataCreate().createPVStructure(fieldBuilder.createStructure());
            for (String fieldName : argMap.keySet()) {
                arguments.getStringField(fieldName).put(argMap.get(fieldName));
            }

            System.out.println(pv + (argMap.isEmpty() ? "" : " " + argMap));

            try {
                PVStructure result = client.request(arguments, 3.0);
                System.out.println(result);
            } catch (RPCRequestException rre) {
                System.out.println(rre.getMessage());
            }
            client.destroy();

        } finally {
            org.epics.pvaccess.ClientFactory.stop();
        }
    }
}
