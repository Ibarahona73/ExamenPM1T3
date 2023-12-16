package com.example.examenpm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.afollestad.materialdialogs.MaterialDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Listado extends AppCompatActivity {

    private Button regresarButton,btnVerReproducir;
    private RadioButton modificarRadioButton,eliminarRadioButton;
    private ListView listado;
    private FirebaseFirestore mFirestore;
    private ArrayAdapter<personas> arrayAdapter;
    private List<personas> entrevistados = new ArrayList<>();
    private ImageView itmImageView;
    private MediaPlayer mediaPlayer;


    //Ivan Barahona 201930010221
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado);

        Intent intent = new Intent(this, MainActivity.class);

        mediaPlayer = new MediaPlayer();
        mFirestore = FirebaseFirestore.getInstance();

        itmImageView = findViewById(R.id.itemImageView);
        btnVerReproducir = findViewById(R.id.btnVerReproducir);
        regresarButton = findViewById(R.id.back);
        modificarRadioButton = findViewById(R.id.radioMod);
        eliminarRadioButton = findViewById(R.id.radioDel);
        listado = findViewById(R.id.lista);

        // Configura el listview y el LayoutManager
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        listado.setAdapter(arrayAdapter);

        DialogHelper.showInstructionsDialog(this);
        cargarDatosDesdeFirestore();

        btnVerReproducir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogoVerReproducir();
            }
        });

        regresarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intent);
            }
        });

        listado.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                position--; // Ajusta la posición
                if (modificarRadioButton.isChecked()) {
                    // Si el RadioButton de modificar está seleccionado
                    abrirModificarActivity(position);

                } else if (eliminarRadioButton.isChecked()) {
                    // Si el RadioButton de Eliminar está seleccionado
                    eliminarEntrevista(position);


                } else {
                    Toast.makeText(Listado.this, "Selecciona uno de los Radio Botones o El Boton" +
                            "\' Ver y Reproducir\'", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    private void abrirModificarActivity(int position) {
        Intent intent = new Intent(Listado.this, Modificar.class);

        // Obtén la persona seleccionada
        personas entrevistado = arrayAdapter.getItem(position);

        // Pasa los datos a la actividad Modificar
        if (entrevistado != null) {
            intent.putExtra("idOrden", entrevistado.getIdOrden());
            intent.putExtra("periodista", entrevistado.getPeriodista());
            intent.putExtra("descripcion", entrevistado.getDescripcion());
            intent.putExtra("fecha", entrevistado.getFecha());
            intent.putExtra("foto", entrevistado.getFoto());
            intent.putExtra("audio", entrevistado.getAudio());

            // Inicia la actividad Modificar
            startActivity(intent);
        }
    }

    private void mostrarDialogoVerReproducir() {
        // Configurar el AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ingrese un número");

        // Crear un EditText para que el usuario ingrese el número
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        // Configurar el botón "Aceptar" del AlertDialog
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    // Obtener el número ingresado por el usuario
                    Log.d("Listado", "Tamaño de la lista: " + arrayAdapter.getCount());
                    String numeroIngresado = input.getText().toString();

                    // Verificar que el número ingresado sea válido
                    if (!numeroIngresado.isEmpty()) {
                        int numero = Integer.parseInt(numeroIngresado);

                        // Buscar la persona en la lista según el número ingresado
                        if (numero >= 0 && numero < arrayAdapter.getCount()) {
                            personas entrevistado = arrayAdapter.getItem(numero);

                            // Mostrar la imagen y reproducir el audio
                            if (entrevistado != null) {
                                showImage(entrevistado.getFoto());
                                playAudio(entrevistado.getAudio());
                            }
                        } else {
                            Toast.makeText(Listado.this, "Número fuera de rango", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(Listado.this, "Ingrese un número válido", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    Toast.makeText(Listado.this, "Ingrese un número válido", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(Listado.this, "Error inesperado", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Configurar el botón "Cancelar" del AlertDialog
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Mostrar el AlertDialog
        builder.show();
    }


    private void showImage(String imageUrl) {
        Log.d("Listado", "URL de la imagen: " + imageUrl);

        Glide.with(this)
                .load(imageUrl)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Log.e("Listado", "Error al cargar la imagen: " + e.getMessage());
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        Log.d("Listado", "Imagen cargada con éxito");
                        return false;
                    }
                })
                .into(itmImageView);
    }

    private void playAudio(String audioUrl) {
        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
            mediaPlayer.setDataSource(audioUrl);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al reproducir el audio", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
    }

    private void cargarDatosDesdeFirestore() {
        CollectionReference entrevistasCollection = mFirestore.collection("Entrevistas");

        entrevistasCollection.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<personas> entrevistadosNuevos = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            personas entrevista = document.toObject(personas.class);
                            entrevistadosNuevos.add(entrevista);
                        }
                        // Actualiza el ArrayAdapter con los nuevos datos
                        arrayAdapter.clear();
                        arrayAdapter.addAll(entrevistadosNuevos);
                        arrayAdapter.notifyDataSetChanged();
                    } else {
                        // Manejar el error
                    }
                });
    }

    private void eliminarEntrevista(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¿Está seguro de eliminar esta entrevista?");

        // Configurar el botón "Sí" del AlertDialog
        builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Realizar la eliminación en Firebase
                eliminarEntrevistaFirebase(position);
            }
        });

        // Configurar el botón "No" del AlertDialog
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Mostrar el AlertDialog
        builder.show();
    }

    private void mostrarDialogoConfirmarEliminacion(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¿Está seguro de eliminar esta entrevista?");

        // Configurar el botón "Sí" del AlertDialog
        builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Realizar la eliminación en Firebase
                eliminarEntrevistaFirebase(position);
            }
        });

        // Configurar el botón "No" del AlertDialog
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Mostrar el AlertDialog
        builder.show();
    }

    private void eliminarEntrevistaFirebase(final int position) {
        CollectionReference entrevistasCollection = mFirestore.collection("Entrevistas");
        personas entrevistado = arrayAdapter.getItem(position);

        if (entrevistado != null) {
            // Obtén el ID de la entrevista y elimínala de Firebase
            String entrevistaId = entrevistado.getIdOrden();
            entrevistasCollection.document(entrevistaId)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(Listado.this, "Entrevista eliminada correctamente", Toast.LENGTH_SHORT).show();

                            // No necesitas ajustar la posición aquí
                            // Actualiza la lista después de la eliminación
                            cargarDatosDesdeFirestore();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Listado.this, "Error al eliminar la entrevista", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

}

