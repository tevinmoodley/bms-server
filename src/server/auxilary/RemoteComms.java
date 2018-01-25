/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.auxilary;

import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import com.mailjet.client.resource.Emailv31;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import server.controllers.CounterController;
import server.exceptions.InvalidBusinessObjectException;
import server.model.BusinessObject;
import server.model.Counter;
import server.model.Employee;
import server.model.FileMetadata;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.AbstractMap;
import java.util.ArrayList;

/**
 *
 * @author ghost
 */
public class RemoteComms
{
    public static String host = "http://192.168.0.103:8080";//192.168.0.103//95.85.57.110
    public static final String TAG = "RemoteComms";
    public static String DB_IP = "localhost";
    public static int DB_PORT = 27017;
    public static String DB_NAME = "fadulousbms";
    public static int TTL = 60*60*2;//2 hours in sec
    public static String SYSTEM_EMAIL = "bms@omegafs.co.za";

    public static String sendGetRequest(String url, ArrayList<AbstractMap.SimpleEntry<String,String>> headers) throws IOException
    {
        IO.log(TAG, IO.TAG_INFO, String.format("\nGET %s HTTP/1.1\nHost: %s", url, host));

        URL urlConn = new URL(host + url);
        HttpURLConnection httpConn =  (HttpURLConnection)urlConn.openConnection();
        for(AbstractMap.SimpleEntry<String,String> header:headers)
            httpConn.setRequestProperty(header.getKey() , header.getValue());
        
        String response = null;
        if(httpConn.getResponseCode() == HttpURLConnection.HTTP_OK)
        {
            response="";
            BufferedReader in = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
            String line="";
            int read=0;
            while ((line=in.readLine())!=null)
                response += line;
            //Log.d(TAG,response);
        }else
        {
            response="";
            BufferedReader in = new BufferedReader(new InputStreamReader(httpConn.getErrorStream()));
            String line="";
            int read=0;
            while ((line=in.readLine())!=null)
                response += line;
            IO.logAndAlert("GET Error", response, IO.TAG_ERROR);
        }

        IO.log(TAG, IO.TAG_INFO, "GET response> " + response + "\n");
        return response;
    }

    public static byte[] sendFileRequest(String file_url, ArrayList<AbstractMap.SimpleEntry<String,String>> headers) throws IOException
    {
        IO.log(TAG, IO.TAG_INFO, String.format("\nGET %s HTTP/1.1", file_url));

        URL urlConn = new URL(host + file_url);
        //URL urlConn = new URL("http://127.0.0.1:9000/api/file/inspection/3-demolition.pdf");
        try(InputStream in = urlConn.openStream())
        {
            //Files.copy(in, new File("download.pdf").toPath(), StandardCopyOption.REPLACE_EXISTING);
            //DataInputStream dataInputStream = new DataInputStream(in);


            ByteArrayOutputStream outbytes = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int read=0;
            while ((read=in.read(buffer, 0, buffer.length))>0)
                outbytes.write(buffer, 0, read);
            outbytes.flush();
            in.close();
            IO.log(TAG, IO.TAG_INFO, "GET received file> " + file_url + " " + outbytes.toByteArray().length + " bytes.\n");
            return outbytes.toByteArray();
        }
        //URL urlConn = new URL(host);
        /*HttpURLConnection httpConn =  (HttpURLConnection)urlConn.openConnection();

        for(AbstractMap.SimpleEntry<String,String> header:headers)
            httpConn.setRequestProperty(header.getKey() , header.getValue());


        String response = null;
        if(httpConn.getResponseCode() == HttpURLConnection.HTTP_OK)
        {
            response="";
            DataInputStream in = new DataInputStream(httpConn.getInputStream());

            ByteArrayOutputStream outbytes = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int read=0;
            while ((read=in.read(buffer, 0, buffer.length))>0)
            {
                outbytes.write(buffer, 0, read);
            }
            outbytes.flush();
            in.close();
            IO.log(TAG, IO.TAG_INFO, "GET received file> " + filename + " " + outbytes.toByteArray().length + "bytes.\n");
            return outbytes.toByteArray();
        }else
        {
            IO.log(TAG, IO.TAG_ERROR, IO.readStream(httpConn.getErrorStream()));
            return null;
        }*/
    }
    
    public static HttpURLConnection postData(String function, ArrayList<AbstractMap.SimpleEntry<String,String>> params, ArrayList<AbstractMap.SimpleEntry<String,String>> headers) throws IOException
    {
        URL urlConn = new URL(host + function);
        HttpURLConnection httpConn = (HttpURLConnection)urlConn.openConnection();
        if(headers!=null)
            for(AbstractMap.SimpleEntry<String,String> header:headers)
                httpConn.setRequestProperty(header.getKey() , header.getValue());
        httpConn.setReadTimeout(10000);
        httpConn.setConnectTimeout(15000);
        httpConn.setRequestMethod("POST");
        httpConn.setDoInput(true);
        httpConn.setDoOutput(true);

        //Encode body data in UTF-8 charset
        StringBuilder result = new StringBuilder();
        for(int i=0;i<params.size();i++)
        {
            AbstractMap.SimpleEntry<String,String> entry = params.get(i);
            if(entry!=null)
            {
                if(entry.getKey()!=null && entry.getValue()!=null)
                {
                    result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                    result.append("=");
                    result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                    result.append((i != params.size() - 1 ? "&" : ""));
                }else return null;
            }else return null;
        }

        IO.log(TAG, IO.TAG_INFO, String.format("POST %s HTTP/1.1\nHost: %s", function, host));

        //Write to server
        OutputStream os = httpConn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));
        writer.write(result.toString());
        writer.flush();
        writer.close();
        os.close();

        //httpConn.connect();
        
        /*Scanner scn = new Scanner(new InputStreamReader(httpConn.getErrorStream()));
        String resp = "";
        while(scn.hasNext())
            resp+=scn.nextLine();
        System.err.println(resp);*
        String resp = httpConn.getHeaderField("Set-Cookie");
        System.err.println(resp);*/
        
        return httpConn;
    }

    public static HttpURLConnection postData(String function, String object, ArrayList<AbstractMap.SimpleEntry<String,String>> headers) throws IOException
    {
        URL urlConn = new URL(host + function);
        HttpURLConnection httpConn = (HttpURLConnection)urlConn.openConnection();
        if(headers!=null)
            for(AbstractMap.SimpleEntry<String,String> header:headers)
                httpConn.setRequestProperty(header.getKey() , header.getValue());
        httpConn.setReadTimeout(10000);
        httpConn.setConnectTimeout(15000);
        httpConn.setRequestMethod("POST");
        httpConn.setDoInput(true);
        httpConn.setDoOutput(true);

        IO.log(TAG, IO.TAG_INFO, String.format("POST %s HTTP/1.1\nHost: %s", function, host));

        //Write to server
        OutputStream os = httpConn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));
        writer.write(object);
        writer.flush();
        writer.close();
        os.close();

        return httpConn;
    }

    public static void uploadFile(String endpoint, ArrayList<AbstractMap.SimpleEntry<String,String>> headers, byte[] file) throws IOException
    {
        URL urlConn = new URL(host + endpoint);
        HttpURLConnection httpConn = (HttpURLConnection)urlConn.openConnection();
        if(headers!=null)
            for(AbstractMap.SimpleEntry<String,String> header:headers)
                httpConn.setRequestProperty(header.getKey() , header.getValue());

        httpConn.setRequestProperty("Content-Length", String.valueOf(file.length));
        httpConn.setReadTimeout(10000);
        httpConn.setConnectTimeout(15000);
        httpConn.setRequestMethod("POST");
        httpConn.setDoInput(true);
        httpConn.setDoOutput(true);

        IO.log(TAG, IO.TAG_INFO, String.format("POST %s HTTP/1.1\nHost: %s", endpoint, host));

        //Write to server
        OutputStream os = httpConn.getOutputStream();
        //OutputStreamWriter writer = new OutputStreamWriter(os);
        os.write(file);
        os.flush();
        os.close();

        httpConn.connect();
        String desc = IO.readStream(httpConn.getInputStream());
        IO.log(RemoteComms.class.getName(), httpConn.getResponseCode() + ":\t" + desc, IO.TAG_INFO);
        httpConn.disconnect();
    }

    public static String commitBusinessObjectToDatabase(BusinessObject businessObject, String collection, String timestamp_name)
    {
        if(businessObject!=null)
        {
            String[] is_valid = businessObject.isValid();
            if(is_valid==null)
            {
                IO.log(RemoteComms.class.getName(),IO.TAG_INFO, "invalid isValid() response from BusinessObject{"+businessObject.getClass().getName()+"}");
                return null;
            }
            if(is_valid.length!=2)
            {
                IO.log(RemoteComms.class.getName(),IO.TAG_INFO, "invalid isValid() response array length from BusinessObject{"+businessObject.getClass().getName()+"}");
                return null;
            }
            if (is_valid[0].toLowerCase().equals("true"))
            {
                IO.log(RemoteComms.class.getName(),IO.TAG_INFO, is_valid[1]);//print message from isValid() call
                IO.log(RemoteComms.class.getName(),IO.TAG_INFO, "committing BusinessObject{"+businessObject.getClass().getName()+"}: "+businessObject.toString());
                //get collection count
                long count = IO.getInstance().mongoOperations().count(null, collection);
                //use collection count as object_number for new BusinessObject
                businessObject.setObject_number(count);//set current collection count as object_number for new BusinessObject
                //commit BusinessObject to DB server
                if(collection!=null)
                    IO.getInstance().mongoOperations().save(businessObject, collection);
                else return null;
                IO.log(RemoteComms.class.getName(),IO.TAG_INFO, "committed BusinessObject:{"+businessObject.getClass().getName()+"} ["+businessObject.get_id()+"]");
                //update respective timestamp
                if(timestamp_name!=null)
                    CounterController.commitCounter(new Counter(timestamp_name, System.currentTimeMillis()));
                else return null;
                return businessObject.get_id();
            } else throw new InvalidBusinessObjectException(is_valid[1]);
        } else throw new InvalidBusinessObjectException("invalid[null] BusinessObject.");
    }

    /**
     *
     * @param subject email subject
     * @param message email message
     * @param recipient_employees recipient Employees
     * @param fileMetadata email attachment files
     * @return Mailjet email send response object
     * @throws MailjetSocketTimeoutException
     * @throws MailjetException
     */
    public static MailjetResponse emailWithAttachment(String subject, String message, Employee[] recipient_employees, FileMetadata[] fileMetadata) throws MailjetSocketTimeoutException, MailjetException
    {
        MailjetClient client;
        MailjetRequest request;
        MailjetResponse response;

        //setup recipients
        JSONArray recipients = new JSONArray();
        for(Employee recipient:recipient_employees)
            recipients.put(new JSONObject()
                    .put("Email", recipient.getEmail())
                    .put("Name", recipient.getFirstname()+" "+recipient.getLastname()));

        //setup files to be emailed
        JSONArray files = new JSONArray();
        for(FileMetadata file: fileMetadata)
            files.put(new JSONObject()
                    .put("ContentType", file.getContent_type())
                    .put("Filename", file.getFilename())
                    .put("Base64Content", file.getFile()));//"VGhpcyBpcyB5b3VyIGF0dGFjaGVkIGZpbGUhISEK"


        client = new MailjetClient("f8d3d1d74c95250bb2119063b3697082", "8304b30da4245632c878bf48f1d65d92", new ClientOptions("v3.1"));
        request = new MailjetRequest(Emailv31.resource)
                .property(Emailv31.MESSAGES, new JSONArray()
                        .put(new JSONObject()
                                .put(Emailv31.Message.FROM, new JSONObject()
                                        .put("Email", SYSTEM_EMAIL)
                                        .put("Name", "BMS"))
                                .put(Emailv31.Message.TO, recipients)
                                .put(Emailv31.Message.SUBJECT, subject)
                                //.put(Emailv31.Message.TEXTPART, "Dear passenger 1, welcome to Mailjet! May the delivery force be with you!")
                                .put(Emailv31.Message.HTMLPART, message)
                                .put(Emailv31.Message.ATTACHMENTS, files)));
        response = client.post(request);
        System.out.println(response.getStatus());
        System.out.println(response.getData());
        return response;
    }

    /**
     *
     * @param subject email subject
     * @param message email message
     * @param recipient_addresses recipient email addresses
     * @param fileMetadata email attachment files
     * @return Mailjet email send response object
     * @throws MailjetSocketTimeoutException
     * @throws MailjetException
     */
    public static MailjetResponse emailWithAttachment(String subject, String message, String[] recipient_addresses, FileMetadata[] fileMetadata) throws MailjetSocketTimeoutException, MailjetException
    {
        MailjetClient client;
        MailjetRequest request;
        MailjetResponse response;

        //setup recipients
        JSONArray recipients_json = new JSONArray();
        for(String recipient:recipient_addresses)
        {
            recipients_json.put(new JSONObject()
                    .put("Email", recipient));
                    //.put("Name", recipient));//recipient.getFirstname()+" "+recipient.getLastname()
        }

        //setup files to be emailed
        JSONArray files = new JSONArray();
        for(FileMetadata file: fileMetadata)
            files.put(new JSONObject()
                    .put("ContentType", file.getContent_type())
                    .put("Filename", file.getFilename())
                    .put("Base64Content", file.getFile()));//"VGhpcyBpcyB5b3VyIGF0dGFjaGVkIGZpbGUhISEK"


        client = new MailjetClient("f8d3d1d74c95250bb2119063b3697082", "8304b30da4245632c878bf48f1d65d92", new ClientOptions("v3.1"));
        request = new MailjetRequest(Emailv31.resource)
                .property(Emailv31.MESSAGES, new JSONArray()
                        .put(new JSONObject()
                                .put(Emailv31.Message.FROM, new JSONObject()
                                        .put("Email", SYSTEM_EMAIL)
                                        .put("Name", "BMS"))
                                .put(Emailv31.Message.TO, recipients_json)
                                .put(Emailv31.Message.SUBJECT, subject)
                                //.put(Emailv31.Message.TEXTPART, "Dear passenger 1, welcome to Mailjet! May the delivery force be with you!")
                                .put(Emailv31.Message.HTMLPART, message)
                                .put(Emailv31.Message.ATTACHMENTS, files)));
        response = client.post(request);
        IO.log(RemoteComms.class.getName(), IO.TAG_INFO, String.valueOf(response.getStatus()));
        IO.log(RemoteComms.class.getName(), IO.TAG_INFO, String.valueOf(response.getData()));
        return response;
    }
}
