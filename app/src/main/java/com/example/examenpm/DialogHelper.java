package com.example.examenpm;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
public class DialogHelper {
    //Ivan Barahona 201930010221
    public static void showInstructionsDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Instrucciones")
                .setMessage("1. Click En El Boton \'Ver Y Reproducir\' Para Ver La Imagen Del Entrevistado " +
                        "Y Oir Su Entrevista  \n\n" +
                        "2. Selecciona Eliminar Luego El Item A Eliminar \n\n" +
                        "3. Selecciona Modificar Luego El Item A Modificar\n\n" +
                        "*Nota: La Lista Empieza Con Indice 0")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
