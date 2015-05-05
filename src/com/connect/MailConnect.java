package com.connect;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.mail.*;
import javax.mail.internet.InternetHeaders;
import javax.mail.search.FlagTerm;

public class MailConnect extends Thread{
	
	Properties props=null;
	Session session=null;
	Store store=null;
	Folder folder=null;
	String mailService="";//"imap-mail.outlook.com";
	String username, pass, unkid;
	DatabaseConnection db;

	public MailConnect(String uname,String pass, String unid) {
		// TODO Auto-generated constructor stub
		
		db=new DatabaseConnection();
		this.username=uname.trim();
		this.pass=pass.trim();
		unkid=unid;
		
		System.out.println("username in mail connect is: "+ username + "///////////////////////");
		System.out.println("password is: "+pass);
		String query="select connection_string from service_master where serviceunkid="+unkid;
		
		try {
			ResultSet rs=db.selectDb(query);
			
			if(rs.next())
				mailService=rs.getString("connection_string").trim();
			
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		props = new Properties();
		username=null;
		
		props.setProperty("mail.store.protocol", "imaps");
		session = Session.getInstance(props, null);
		//new GMailAuthenticator("akshar.joshi91@gmail.com", "navy4242"));
		
		
		
        try {
			store = session.getStore();
			System.out.println("mail serice is: " + mailService);
			
			store.connect(mailService,"akshar.joshi91@gmail.com", "navy4242");
			//rs.close();
	        
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        catch(Exception e2) {
        	e2.printStackTrace();
        }
		
	}
		
	public void run(){
		ScheduledExecutorService executor =
			    Executors.newSingleThreadScheduledExecutor();

			Runnable periodicTask = new Runnable() {
			    public void run() {
			        // Invoke method(s) to do the work
			    	connect();
			    }
			};
			executor.scheduleAtFixedRate(periodicTask, 0, 60, TimeUnit.SECONDS);
	}

	/*public void selectService() {
        System.out.println("in select service");
        
        String query="select serviceunkid from service_service where clientunkid in (select clientunkid from client_master where username='akshar');";

       /*
        try {
         	System.out.println("Before session");
            session = Session.getInstance(props, null);
            store = session.getStore();
            System.out.println("here");
        }
        catch(Exception e){
        	e.printStackTrace();
        } */
        
	//}
	
	public void connect() {
		//props = new Properties();
        System.out.println("here before prop");

        //props.setProperty("mail.store.protocol", "imaps");
        try {
        	/*System.out.println("Before session");
            session = Session.getInstance(props, null);
            store = session.getStore();
            System.out.println("here");
            */
        	
        	System.out.println("For "+username+" ***************************");
        	System.out.println("connected");
            folder = store.getFolder("INBOX");
            folder.open(Folder.READ_ONLY);
            System.out.println("read");
            
            FlagTerm ft = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
            Message[] UNReadmessages = folder.search(ft);
            
            System.out.println("unread: "+(folder.getMessageCount()-UNReadmessages.length));

            Message msg = folder.getMessage(folder.getMessageCount());
            System.out.println("count is : "+folder.getMessageCount());
            
            Address[] in = msg.getFrom();
            String from=null;
            
            for (Address address : in) {
                System.out.println("FROM:" + address.toString());
                from=address.toString();
            }
            
            if(from.indexOf('<')>0)
            	from=from.substring(from.indexOf('<')+1,from.indexOf('>'));
            
            System.out.println("from value is: "+from);
            
            Object o=msg.getContent();
            BodyPart bp = null;
            
            String cont,sub;
            
            if(o instanceof String) {
            	System.out.println("%%%%%%%%%%%%%%%%%%% HERE %%%%%%%%%%%%%%%%%%");
            	System.out.println("SENT DATE:" + msg.getSentDate());
                System.out.println("SUBJECT:" + msg.getSubject());
                
                System.out.println("CONTENT:" + (String)o);
                
                cont=(String)o;
                sub=msg.getSubject();
            }
            else {
            	Multipart mp = (Multipart) msg.getContent();
                
            	 bp= mp.getBodyPart(0);
                System.out.println("&&&&&&&&&&&&&&&&&&&&& THERE &&&&&&&&&&&&&&&&&&&&&&&&"); 
                System.out.println("SENT DATE:" + msg.getSentDate());
                System.out.println("SUBJECT:" + msg.getSubject());
                
                //System.out.println("CONTENT:" + (String)o);
                System.out.println("CONTENT:" + bp.getContent());
                cont=bp.getContent().toString();
                sub=msg.getSubject();
                
            }
              
            
            
            
            System.out.println("content to string is: "+cont);
            
            SendSMS s=new SendSMS();
            
            System.out.println("before check");
            
            if(s.checkFrom(unkid,from,msg.getSubject())) {
            	System.out.println("message sent");
            }
            else {
            	System.out.println("mail from unknown: "+from);
            	s.runCheckSend(unkid,cont);            	
            }
            
            System.out.println("after check........");
            /*s.checkBlock(unkid);
            s.runCheckSend(unkid);
            */
            folder.close(false);
          //  inbox.close();
        } catch (Exception mex) {
            mex.printStackTrace();
        }
	}
	
	
}