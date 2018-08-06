package com.philips.alexa.apihubinvoker;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.OutputSpeech;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.amazon.speech.ui.SsmlOutputSpeech;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
 
public class SayHelloSpeechlet implements Speechlet
{
    public SpeechletResponse onLaunch(final LaunchRequest request, final Session session)
        throws SpeechletException
    {
        System.out.println("onLaunch requestId={}, sessionId={} " + request.getRequestId()
                            + " - " + session.getSessionId());
        return getWelcomeResponse();
    }
 
    public SpeechletResponse onIntent(final IntentRequest request, final Session session)
        throws SpeechletException
    {
        System.out.println("onIntent requestId={}, sessionId={} " + request.getRequestId()
                            + " - " + session.getSessionId());
 
        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;
 
        System.out.println("intentName : " + intentName);
 
        if ("SayHelloIntent".equals(intentName)) {
            return getHelloResponse();
        } 
        
        else if ("RunJobIntent".equals(intentName)) {
            return getRunJobResponse(intent);
        } 
        
        else if ("GetContactIntent".equals(intentName)) {
            return getGetContactResponse(intent);
        } 
        
        else if ("GetOpportunityIntent".equals(intentName)) {
            return getGetOpportunityResponse(intent);
        } 
        
        else if ("AMAZON.StopIntent".equals(intentName)) {
            return getStopResponse();
        } 
        

        else if ("AMAZON.CancelIntent".equals(intentName)) {
            return getStopResponse();
        } 
        
        
        else if ("AMAZON.HelpIntent".equals(intentName)) {
            return getHelpResponse();
        } 
        
        else {
            return getHelpResponse();
        }
    }
 
    
    
    
    
    
    /**
     * Creates and returns a {@code SpeechletResponse} with a welcome message.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getWelcomeResponse()
    {
    	 //String speechText = "<speak><s>Welcome to the API Hub</s> <s>you can ask me to execute Master Data Management , Data Integration Hub or Power Center Jobs<s>You can also ask me to fetch contact details of a person</s></speak>";
    	//String speechText = "Welcome to the API Hub you can ask me to execute Master Data Management , Data Integration Hub or Power Center Jobs You can also ask me to fetch contact details of a person";
    	
    	String speechText = "Welcome to the API Hub. You can ask me to fetch Opportunities open for a Company, or even contact details of a person ";
    	
 
        // Create the Simple card content.
 
        SimpleCard card = new SimpleCard();
        card.setTitle("API Hub");
        card.setContent(speechText);
 
        // Create the plain text output.
 
 
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);
        
        //SsmlOutputSpeech speech = new SsmlOutputSpeech();
        //speech.setSsml(speechText);
 
        
        // Create reprompt
 
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);
 
        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }
 

    
    
    private SpeechletResponse getGetOpportunityResponse(Intent intent) 
    {
        String speechText = "" ;
 
        // Create the Simple card content.
 
        SimpleCard card = new SimpleCard();
        card.setTitle("API Hub");
        card.setContent(speechText);
        
        // Let us put the content into S3 Bucket first
        
        Map<String, Slot> myslots =intent.getSlots();
        String sOut ="";
        String sCompanyName="";
        
        for (Map.Entry<String, Slot> entry : myslots.entrySet()) 
        {
        	System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue());
        	Slot tempslot =entry.getValue();
        	
        	if (tempslot.getName().equals("CompanyName")){
        		sCompanyName=tempslot.getValue() ;
        	}
        	
   	    	}

    	sOut ="SFDC" +",OPPORTUNITY," + sCompanyName;
    	
    	
       BasicAWSCredentials basicCred = new BasicAWSCredentials("XXXXX", "YYYYY"); 
		
		AmazonS3 s3 = new AmazonS3Client(basicCred);
		
       
       
       StringBuilder stringBuilder = new StringBuilder();

       //Writing in file
       stringBuilder.append(sOut);

       // Upload file
       ByteArrayInputStream inputStream = null;
       
       inputStream = new ByteArrayInputStream(stringBuilder.toString().getBytes());
       
       
       String bucketName = "com.philips.purush";
		String in_folderName="alexa_inbox";
		String out_folderName="alexa_outbox";
		String SUFFIX="/";
		String  uploadFileName=new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + "_req.txt";
		s3.putObject(bucketName, in_folderName+ SUFFIX +uploadFileName, inputStream, new ObjectMetadata());
		
 
		try {
			Thread.sleep(9000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Now let us wait to get a response in the outbox
		
		
		ListObjectsRequest param = new ListObjectsRequest() ;
		param.setBucketName(bucketName);
		param.setPrefix(out_folderName);
		
		String sFileName =uploadFileName.replace("_req.txt", "_resp.txt");
	  	String filecontent="";		
		
	  	System.out.println("Looking for the following file::" + sFileName);
		
		ObjectListing lst = s3.listObjects(param);
		
		for ( S3ObjectSummary summ : lst.getObjectSummaries()) {
		  
		  if (summ.getKey().contains(sFileName)){
			  GetObjectRequest param2 = new GetObjectRequest(bucketName, out_folderName+ SUFFIX  + sFileName);
			  S3Object fullObject= s3.getObject(param2);
			  BufferedReader reader = new BufferedReader(new InputStreamReader(fullObject.getObjectContent()));
				  	String line = null;
				  	try {
						while ((line = reader.readLine()) != null) 
						{
							filecontent=filecontent+ line;
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				  	System.out.println(filecontent);
				  	DeleteObjectRequest param3 = new DeleteObjectRequest(bucketName, out_folderName+ SUFFIX  + sFileName);
					s3.deleteObject(param3);
			}
		}
// Create the plain text output.
 
		
		SsmlOutputSpeech speech = new SsmlOutputSpeech();
        speech.setSsml(filecontent);
        
        // Create reprompt
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);
        
        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }

    
    
    private SpeechletResponse getGetContactResponse(Intent intent) 
    {
        String speechText = "" ;
 
        // Create the Simple card content.
 
        SimpleCard card = new SimpleCard();
        card.setTitle("API Hub");
        card.setContent(speechText);
        
        // Let us put the content into S3 Bucket first
        
        Map<String, Slot> myslots =intent.getSlots();
        String sOut ="";
        String sContactName="";
        
        for (Map.Entry<String, Slot> entry : myslots.entrySet()) 
        {
        	System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue());
        	Slot tempslot =entry.getValue();
        	
        	if (tempslot.getName().equals("ContactName")){
        		sContactName=tempslot.getValue() ;
        	}
        	
   	    	}

    	sOut ="SFDC" +",CONTACT," + sContactName;
    	
    	
       BasicAWSCredentials basicCred = new BasicAWSCredentials("XXXXXX", "YYYYYY"); 
		
		AmazonS3 s3 = new AmazonS3Client(basicCred);
		
       
       
       StringBuilder stringBuilder = new StringBuilder();

       //Writing in file
       stringBuilder.append(sOut);

       // Upload file
       ByteArrayInputStream inputStream = null;
       
       inputStream = new ByteArrayInputStream(stringBuilder.toString().getBytes());
       
       
       String bucketName = "com.philips.purush";
		String in_folderName="alexa_inbox";
		String out_folderName="alexa_outbox";
		String SUFFIX="/";
		String  uploadFileName=new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + "_req.txt";
		s3.putObject(bucketName, in_folderName+ SUFFIX +uploadFileName, inputStream, new ObjectMetadata());
		
 
		try {
			Thread.sleep(9000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Now let us wait to get a response in the outbox
		
		
		ListObjectsRequest param = new ListObjectsRequest() ;
		param.setBucketName(bucketName);
		param.setPrefix(out_folderName);
		
		String sFileName =uploadFileName.replace("_req.txt", "_resp.txt");
	  	String filecontent="";		
		
	  	System.out.println("Looking for the following file::" + sFileName);
		
		ObjectListing lst = s3.listObjects(param);
		
		for ( S3ObjectSummary summ : lst.getObjectSummaries()) {
		  
		  if (summ.getKey().contains(sFileName)){
			  GetObjectRequest param2 = new GetObjectRequest(bucketName, out_folderName+ SUFFIX  + sFileName);
			  S3Object fullObject= s3.getObject(param2);
			  BufferedReader reader = new BufferedReader(new InputStreamReader(fullObject.getObjectContent()));
				  	String line = null;
				  	try {
						while ((line = reader.readLine()) != null) 
						{
							filecontent=filecontent+ line;
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				  	System.out.println(filecontent);
				  	DeleteObjectRequest param3 = new DeleteObjectRequest(bucketName, out_folderName+ SUFFIX  + sFileName);
					s3.deleteObject(param3);
			}
		}
// Create the plain text output.
 
		
		SsmlOutputSpeech speech = new SsmlOutputSpeech();
        speech.setSsml(filecontent);
        
        // Create reprompt
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);
        
        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }
 
    
 
    private SpeechletResponse getRunJobResponse(Intent intent) 
    {
        String speechText = "" ;
 
        // Create the Simple card content.
 
        SimpleCard card = new SimpleCard();
        card.setTitle("API Hub");
        card.setContent(speechText);
        
        // Let us put the content into S3 Bucket first
        
        Map<String, Slot> myslots =intent.getSlots();
        String sOut ="";
        String sEnv="";
        String sJobType ="";
        String sJobName ="";
        
        for (Map.Entry<String, Slot> entry : myslots.entrySet()) 
        {
        	System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue());
        	Slot tempslot =entry.getValue();
        	
        	if (tempslot.getName().equals("environment")){
        		sEnv=tempslot.getValue() ;
        	}
        	
        	if (tempslot.getName().equals("jobtype")){
        		sJobType=tempslot.getValue() ;
        	}
        	

        	if (tempslot.getName().equals("jobname")){
        		sJobName=tempslot.getValue() ;
        	}

   	    	}

    	sOut =sJobType +"," + sJobName +"," + sEnv;
    	
    	
       BasicAWSCredentials basicCred = new BasicAWSCredentials("XXXXXXX", "YYYYYY"); 
		
		AmazonS3 s3 = new AmazonS3Client(basicCred);
		
       
       
       StringBuilder stringBuilder = new StringBuilder();

       //Writing in file
       stringBuilder.append(sOut);

       // Upload file
       ByteArrayInputStream inputStream = null;
       
       inputStream = new ByteArrayInputStream(stringBuilder.toString().getBytes());
       
       
       String bucketName = "com.philips.purush";
		String in_folderName="alexa_inbox";
		String out_folderName="alexa_outbox";
		String SUFFIX="/";
		String  uploadFileName=new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + "_req.txt";
		s3.putObject(bucketName, in_folderName+ SUFFIX +uploadFileName, inputStream, new ObjectMetadata());
		
 
		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Now let us wait to get a response in the outbox
		
		
		ListObjectsRequest param = new ListObjectsRequest() ;
		param.setBucketName(bucketName);
		param.setPrefix(out_folderName);
		
		String sFileName =uploadFileName.replace("_req.txt", "_resp.txt");
	  	String filecontent="";		
		
	  	System.out.println("Looking for the following file::" + sFileName);
		
		ObjectListing lst = s3.listObjects(param);
		
		for ( S3ObjectSummary summ : lst.getObjectSummaries()) {
		  
		  if (summ.getKey().contains(sFileName)){
			  GetObjectRequest param2 = new GetObjectRequest(bucketName, out_folderName+ SUFFIX  + sFileName);
			  S3Object fullObject= s3.getObject(param2);
			  BufferedReader reader = new BufferedReader(new InputStreamReader(fullObject.getObjectContent()));
				  	String line = null;
				  	try {
						while ((line = reader.readLine()) != null) 
						{
							filecontent=filecontent+ line;
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				  	System.out.println(filecontent);
				  	DeleteObjectRequest param3 = new DeleteObjectRequest(bucketName, out_folderName+ SUFFIX  + sFileName);
					s3.deleteObject(param3);
			}
		}
// Create the plain text output.
 
        System.out.println(filecontent);
        SsmlOutputSpeech speech = new SsmlOutputSpeech();
        speech.setSsml(filecontent);
        
        
        // Create reprompt
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);
        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }
 
    
    
    
    /**
     * Creates a {@code SpeechletResponse} for the hello intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getHelloResponse()
    {
 //       String speechText = "<speak><s>Welcome to the API Hub</s> <s>you can ask me to execute Master Data Management , Data Integration Hub or Power Center Jobs<s>You can also ask me to fetch contact details of a person</s></speak>";
    	String speechText = "Welcome to the API Hub you can ask me to execute Master Data Management , Data Integration Hub or Power Center Jobs You can also ask me to fetch contact details of a person";
        // Create the Simple card content.
 
        SimpleCard card = new SimpleCard();
        card.setTitle("APIHub");
        card.setContent(speechText);
 
        // Create the plain text output.
 
 
//        SsmlOutputSpeech speech = new SsmlOutputSpeech();
//       speech.setSsml(speechText);
        

		PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
		speech.setText(speechText);
 
        
        return SpeechletResponse.newTellResponse(speech, card);
    }
 
    
    
 
    /**
     * Creates a {@code SpeechletResponse} for the help intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getStopResponse()
    {
        String speechText = "Thank you for using our Service, Hope to see you soon";
 
        // Create the Simple card content.
 
        SimpleCard card = new SimpleCard();
        card.setTitle("APIHub");
        card.setContent(speechText);
 
        // Create the plain text output.
 
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);
 
        // Create reprompt
 
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);
 
        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }
 
    
    /**
     * Creates a {@code SpeechletResponse} for the help intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getHelpResponse()
    {
        String speechText = "You can ask me to execute Master Data Management , Data Integration Hub or even Power Center Jobs";
 
        // Create the Simple card content.
 
        SimpleCard card = new SimpleCard();
        card.setTitle("APIHub");
        card.setContent(speechText);
 
        // Create the plain text output.
 
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);
 
        // Create reprompt
 
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);
 
        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }
 
    public void onSessionStarted(final SessionStartedRequest request, final Session session)
        throws SpeechletException
    {
        System.out.println("onSessionStarted requestId={}, sessionId={} " + request.getRequestId()
                            + " - " + session.getSessionId());
    }
 
    public void onSessionEnded(final SessionEndedRequest request, final Session session)
        throws SpeechletException
    {
        System.out.println("onSessionEnded requestId={}, sessionId={} " + request.getRequestId()
                            + " - " + session.getSessionId());
    }
}
