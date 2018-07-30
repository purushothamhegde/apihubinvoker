package com.philips.alexa.apihubinvoker;

	import java.util.HashSet;
	import java.util.Set;
	import com.amazon.speech.speechlet.Speechlet;
	import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;
	 
	public class SayHelloRequestStreamHandler extends SpeechletRequestStreamHandler {
	 
	    private static final Set<String> supportedApplicationIds;
	 
	    static {
	        /*
	         * This Id can be found on https://developer.amazon.com/edw/home.html#/
	         * "Edit" the relevant Alexa Skill and put the relevant Application Ids
	         * in this Set.
	         */
	        supportedApplicationIds = new HashSet<String>();
	        supportedApplicationIds.add("amzn1.ask.skill.7af1fe22-3ac9-4057-a07f-7fd3914dfdcf");
	        System.out.println("Supported app ids : " + supportedApplicationIds);
	    }
	 
	    public SayHelloRequestStreamHandler() {
	        super(new SayHelloSpeechlet(), supportedApplicationIds);
	    }
	 
	    public SayHelloRequestStreamHandler(Speechlet speechlet, Set<String> supportedApplicationIds) {
	        super(speechlet, supportedApplicationIds);
	    }
	}

