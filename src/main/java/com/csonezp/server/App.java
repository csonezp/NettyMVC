package com.csonezp.server;

import com.csonezp.server.mvc.Dispatcher;
import com.csonezp.server.netty.Server;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.init("com.csonezp.server");
        Server server = new Server();
        try {
            server.server(8080, dispatcher);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
