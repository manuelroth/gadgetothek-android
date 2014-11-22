package ch.manuelroth.gadgetothek_android.library;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import android.os.AsyncTask;
import android.util.Log;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.Future;

import ch.manuelroth.gadgetothek_android.bl.Gadget;
import ch.manuelroth.gadgetothek_android.bl.Loan;
import ch.manuelroth.gadgetothek_android.bl.Reservation;


public class LibraryService {

    public static LoginToken token;
    private static String serverUrl = "http://192.168.1.123:4730";
    private static List<Callback<?>> listeners = new ArrayList<Callback<?>>();

    public static boolean IsLoggedIn()
    {
        return token != null;
    }


    public static void addCallBackListener(Callback<?> callback)
    {
        listeners.add(callback);
    }

    public static void removeCallBackListener(Callback<?> callback)
    {
        if(listeners.contains(callback))
        {
            listeners.remove(callback);
        }
    }

    public static void login(String mail, String password, final Callback<Boolean> callback)
    {
        HashMap<String, String> parameter = new HashMap<String, String>();
        parameter.put("email", mail);
        parameter.put("password", password);
        Request request = new Request(RestType.POST, serverUrl+ "/login", LoginToken.class, parameter, new Callback<LoginToken>() {
            @Override
            public void notfiy(LoginToken input) {
                token = input;
                callback.notfiy(input != null && input.getSecurityToken() != "" );
                notifyCallBackListeners();
            }
        });
        request.execute();
    }

    public static void logout(final Callback<Boolean> callback)
    {
        HashMap<String, String> parameter = new HashMap<String, String>();
        parameter.put("token", getTokenAsString());

        Request request = new Request(RestType.POST, serverUrl+ "/logout", Boolean.class, parameter, new Callback<Boolean>() {
            @Override
            public void notfiy(Boolean input) {
                if(input) {
                    token = null;
                }
                callback.notfiy(input);
                notifyCallBackListeners();
            }
        });
        request.execute();
    }

    public static void register(String mail, String password, String name, String studentenNumber,  final Callback<Boolean> callback) {
        HashMap<String, String> parameter = new HashMap<String, String>();
        parameter.put("email", mail);
        parameter.put("password", password);
        parameter.put("name", name);
        parameter.put("studentnumber", studentenNumber);

        Request request = new Request(RestType.POST, serverUrl+ "/register", Boolean.class, parameter, new Callback<Boolean>() {
            @Override
            public void notfiy(Boolean input) {
                callback.notfiy(input);
                notifyCallBackListeners();
            }
        });
        request.execute();
    }




    public static void getLoansForCustomer(final Callback<List<Loan>> callback)
    {
        if(token == null)
        {
            throw new IllegalStateException("Not logged in");
        }
        HashMap<String, String> parameter = new HashMap<String, String>();

        parameter.put("token", getTokenAsString());
        Request request = new Request(RestType.GET, serverUrl+ "/loans", new TypeToken<List<Loan>>() {}.getType(), parameter,new Callback<List<Loan>>() {
            @Override
            public void notfiy(List<Loan> input) {
                callback.notfiy(input  == null?  new ArrayList<Loan>(): input);
                notifyCallBackListeners();
            }
        } );
        request.execute();
    }

    public static void getReservationsForCustomer(final Callback<List<Reservation>> callback)
    {
        if(token == null)
        {
            throw new IllegalStateException("Not logged in");
        }
        HashMap<String, String> parameter = new HashMap<String, String>();
        parameter.put("token", getTokenAsString());

        Request request = new Request(RestType.GET, serverUrl+ "/reservations", new TypeToken<List<Reservation>>() {}.getType(), parameter,new Callback<List<Reservation>>() {
            @Override
            public void notfiy(List<Reservation> input) {
                callback.notfiy(input  == null?  new ArrayList<Reservation>() : input);
                notifyCallBackListeners();
            }
        } );
        request.execute();
    }


    public static void reserveGadget(Gadget toReserve, final Callback<List<Loan>> callback)
    {
        if(token == null)
        {
            throw new IllegalStateException("Not logged in");
        }
        HashMap<String, String> parameter = new HashMap<String, String>();
        parameter.put("token", getTokenAsString());
        parameter.put("gadgetId", toReserve.getInventoryNumber());

        Request request = new Request(RestType.POST, serverUrl+ "/reservations", new TypeToken<List<Loan>>() {}.getType(), parameter,new Callback<List<Loan>>() {
            @Override
            public void notfiy(List<Loan> input) {
                callback.notfiy(input  == null?  new ArrayList<Loan>(): input);
                notifyCallBackListeners();
            }
        } );
        request.execute();
    }


    public static void deleteReservation(Reservation toDelete, final Callback<Boolean> callback)
    {
        if(token == null)
        {
            throw new IllegalStateException("Not logged in");
        }
        HashMap<String, String> parameter = new HashMap<String, String>();
        parameter.put("token", getTokenAsString());
        parameter.put("id", toDelete.getReservationId());
        Request request = new Request(RestType.DELETE, serverUrl+ "/reservations", Boolean.class, parameter,  new Callback<Boolean>() {
            @Override
            public void notfiy(Boolean input) {
                callback.notfiy(input);
                notifyCallBackListeners();
            }
        });
        request.execute();
    }

    public static void getGadgets(final Callback<List<Gadget>> callback)
    {
        if(token == null)
        {
            throw new IllegalStateException("Not logged in");
        }
        HashMap<String, String> parameter = new HashMap<String, String>();

        parameter.put("token", getTokenAsString());
        Request request = new Request(RestType.GET, serverUrl+ "/gadgets", new TypeToken<List<Gadget>>() {}.getType(), parameter,  new Callback<List<Gadget>>() {
            @Override
            public void notfiy(List<Gadget> input) {
                callback.notfiy(input);
                notifyCallBackListeners();
            }
        });
        request.execute();
    }

    private static String getTokenAsString()
    {
        Gson gson = createGsonObject();
        return gson.toJson(token);
    }


    private static void notifyCallBackListeners()
    {
        for( Callback<?> callback : listeners )
        {
            callback.notfiy(null);
        }
    }

    static Gson createGsonObject()
    {
        return  new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
    }
}

enum RestType
{
    POST,
    GET,
    DELETE,
}


class Request<T> extends AsyncTask<Object, Integer, T> {

    private RestType restType;
    private final String url;
    private final Type typeClass;
    private HashMap<String, String> parameterList;
    private Callback<T> callback;
    private Future<T> future;

    public Request(RestType type, String url, Type typeClass, HashMap<String, String> parameterList, Callback<T> callback) {
        this.restType = type;

        this.url = url;
        this.typeClass = typeClass;
        this.parameterList = parameterList;
        this.callback = callback;
    }

    protected T doInBackground(Object... urls) {
        return getData(url, typeClass);
    }


    private <E> E getData(String url, Type type) {
        Log.d("LibraryService", url  );
        AsyncHttpClient c = new AsyncHttpClient();
        try {

            AsyncHttpClient.BoundRequestBuilder x = null;

            switch(restType) {
                case POST:
                    x = c.preparePost(url);
                    break;
                case GET:
                    x = c.prepareGet(url);
                    break;
                case DELETE:
                    x = c.prepareDelete(url);
                    break;
            }

            for(Map.Entry<String, String> entry :  parameterList.entrySet())
            {
                x.addParameter(entry.getKey(), entry.getValue());
            }

            Future<Response> f = x.execute();

            Response r = f.get();
            Gson gson = LibraryService.createGsonObject();

            E fromJson;
            String text = r.getResponseBody();
            fromJson = gson.fromJson(text, type);
            return fromJson;
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally{
            c.close();
        }
        return null;
    }


    protected void onProgressUpdate(Integer... progress) {

    }

    protected void onPostExecute(T result) {
        callback.notfiy(result);
    }
}
