package com.example.orthodentalsahorario;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.Result;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class salida extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView msZXingScannerView;
    public static String var_user;
    public static String var_hora;
    public static String var_fecha;
    public static String var_bandera = "SALIDA";
    public static String var_direccion = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salida);
        //quitar el navbar temporalmente
        ActionBar bar = getSupportActionBar();
        if (getSupportActionBar() == null) {

        } else {
            getSupportActionBar().hide();
        }

        Bundle parametros = this.getIntent().getExtras();
        if(parametros !=null){
            var_direccion = parametros.getString("direccion");
        }
        Date fechaActual = new Date();
        //Formateando la fecha:
        DateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");
        DateFormat formatoFecha = new SimpleDateFormat("yyyy/MM/dd");
        var_fecha = formatoHora.format(fechaActual);
        var_hora = formatoFecha.format(fechaActual);

        var_user = indusuario();


//        habilitando en navbar automaticamente
        msZXingScannerView = new ZXingScannerView(this);
        setContentView(msZXingScannerView);
        msZXingScannerView.setResultHandler(this);
        msZXingScannerView.startCamera();
    }
    @Override
    public void handleResult(Result result) {
        final Context context = this;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        // set title

        alertDialogBuilder.setTitle("Respuesta");
        alertDialogBuilder.setIcon(R.mipmap._scanner);

        // set dialog message
        alertDialogBuilder
                .setMessage("EXITO ENTRADA")
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, close
                        // current activity
                        String res = result.getText().toString();

                        if(res.equals("ORTHODENTAL")){
                            Toast.makeText(getApplicationContext(), "Exito Dato Registrado", Toast.LENGTH_SHORT).show();
                            Log.d("newValue :", String.valueOf(var_user));
                            Log.d("newValue :", String.valueOf(var_hora));
                            Log.d("newValue :", String.valueOf(var_fecha));
                            Log.d("newValue :", String.valueOf(var_bandera));

                            ejecutarServices("https://www.orthodentalnic.com/accesohorarios/seguridad/ingreso.php");

                        }

                        //grabar(res);
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing

//                        grabar("");
//                        scanner.this.finish();
//                        Intent intent = new Intent(scanner.this, MainActivity.class);
//                        startActivity(intent);
//                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private void ejecutarServices(String url) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.equals("no")) {
                    Toast.makeText(getApplicationContext(), "ACCESO A INTERNET", Toast.LENGTH_SHORT).show();
                } else {
                    finish();
                    Intent intent = new Intent(salida.this, principal.class);
                    startActivity(intent);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

                if (volleyError instanceof TimeoutError || volleyError instanceof NoConnectionError) {
                    Toast.makeText(getApplicationContext(), "No Connection/Communication Error!", Toast.LENGTH_SHORT).show();

                } else if (volleyError instanceof AuthFailureError) {
                    Toast.makeText(getApplicationContext(), "Authentication/ Auth Error!", Toast.LENGTH_SHORT).show();
                } else if (volleyError instanceof ServerError) {
                    Toast.makeText(getApplicationContext(), "Server Error!", Toast.LENGTH_SHORT).show();
                } else if (volleyError instanceof NetworkError) {
                    Toast.makeText(getApplicationContext(), "Network Error!", Toast.LENGTH_SHORT).show();
                } else if (volleyError instanceof ParseError) {
                    Toast.makeText(getApplicationContext(), "Parse Error!", Toast.LENGTH_SHORT).show();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parametross = new HashMap<String, String>();
                parametross.put("user", var_user.toString());
                parametross.put("hora", var_hora.toString());
                parametross.put("fecha", var_fecha.toString());
                parametross.put("entrada", var_bandera.toString());
                parametross.put("direccion", var_direccion.toString());
                return parametross;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }


    public String indusuario() {
        String[] archivos = fileList();
        String todo = "";
        if (existe(archivos, "datos.dll")) {
            try {
                InputStreamReader archivo = new InputStreamReader(
                        openFileInput("datos.dll"));
                BufferedReader br = new BufferedReader(archivo);
                String linea = br.readLine();
                while (linea != null) {
                    todo = todo + linea + "\n";
                    linea = br.readLine();
                }
                br.close();
                archivo.close();
                if (todo.equals("")) {
                    finish();
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                }
            } catch (IOException e) {

            }
        }
        return todo;
    }

    private boolean existe(String[] archivos, String archbusca) {
        for (int f = 0; f < archivos.length; f++) {
            if (archbusca.equals(archivos[f])) {
                return true;
            }
        }
        return false;
    }

}
