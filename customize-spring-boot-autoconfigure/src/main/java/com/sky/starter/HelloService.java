package com.sky.starter;

public class HelloService {

    HelloProperties helloProperties;

    public String sayHello(String name){
        StringBuilder temp = new StringBuilder();
        temp.append(helloProperties.getPrefix());
        temp.append(name);
        temp.append(helloProperties.getSufix());
        System.out.println(temp);
        return temp.toString();
    }

    public HelloProperties getHelloProperties() {
        return helloProperties;
    }

    public void setHelloProperties(HelloProperties helloProperties) {
        this.helloProperties = helloProperties;
    }
}
