package com.example.examenpm;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Modificar extends AppCompatActivity {

    private Button lista, modificar, tomarFoto, tomarAud, playButton;
    private EditText Fecha, Orden, Desc, periodist;

    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 110;
    private static final int REQUEST_STORAGE_PERMISSION = 111;
    private String currentPhotoPath;
    private ImageView perfil;
    private MediaRecorder grabacion;
    private String archivoSalida = null;
    private MediaPlayer mediaPlayer;
    private boolean cancelLongClick = false;
    private boolean isPlaying = false;
    private FirebaseFirestore mfirestore;


    //Ivan Barahona 201930010221
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modificar);

        Intent intento = new Intent(this, Listado.class);
        mfirestore = FirebaseFirestore.getInstance();
        Orden = (EditText) findViewById(R.id.IdOrden);
        Fecha = (EditText) findViewById(R.id.Fecha);
        Desc = (EditText) findViewById(R.id.Desc);
        periodist = (EditText) findViewById(R.id.Periodista);
        perfil = (ImageView) findViewById(R.id.Perfil);
        lista = (Button) findViewById(R.id.btnEntrevista);
        modificar = (Button) findViewById(R.id.btnMod);
        tomarFoto = (Button) findViewById(R.id.btnFoto);
        tomarAud = (Button) findViewById(R.id.btnAud);
        playButton = findViewById(R.id.playButton);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(Calendar.getInstance().getTime());
        Fecha.setText(currentDate);

        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
        }

        // Verificar y solicitar permisos de grabación de audio
        if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        }

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("idOrden")) {
            String idOrden = intent.getStringExtra("idOrden");
            String periodista = intent.getStringExtra("periodista");
            String descripcion = intent.getStringExtra("descripcion");
            String fecha = intent.getStringExtra("fecha");
            String fotoPath = intent.getStringExtra("foto");
            String audioPath = intent.getStringExtra("audio");

            // Llena los campos correspondientes en la interfaz de usuario
            Orden.setText(idOrden);
            periodist.setText(periodista);
            Desc.setText(descripcion);
            Fecha.setText(fecha);

            // Carga la imagen y el audio
            loadImage(fotoPath);
            archivoSalida = audioPath;
        }



        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPlaying) {
                    reproducirAudio();
                } else {
                    detenerReproduccion();

                }
            }
        });

        //Crear los datos
        modificar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Modificar();
            }
        });

        //Tomar la Grabacion de Audio
        tomarAud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iniciarGrabacion();
            }
        });

        //Tomar la Imagen y mostrarla en el ImageView
        tomarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        //ir Al Listado de Entrevistados
        lista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intento);
            }
        });
    }

    //Carga Imagen

    private void loadImage(String imagePath) {
        // Similar a showImage en Listado, utiliza Glide para cargar la imagen en ImageView
        Glide.with(this)
                .load(imagePath)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Log.e("Modificar", "Error al cargar la imagen: " + e.getMessage());
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        Log.d("Modificar", "Imagen cargada con éxito");
                        return false;
                    }
                })
                .into(perfil);
    }



    //Funciones de la Camara



    private String saveMediaFile(String filePath, String directoryName) {
        File mediaStorageDir = new File(getFilesDir(), directoryName);

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "Failed to create directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String mediaFileName = "MEDIA_" + timeStamp;

        File mediaFile;
        if (directoryName.equals("images")) {
            mediaFile = new File(mediaStorageDir, mediaFileName + ".jpg");
        } else if (directoryName.equals("audios")) {
            mediaFile = new File(mediaStorageDir, mediaFileName + ".3gp");
        } else {
            return null; // Unsupported media type
        }

        try {
            copyFile(filePath, mediaFile.getAbsolutePath());
            return mediaFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void copyFile(String sourcePath, String destinationPath) throws IOException {
        File source = new File(sourcePath);
        File destination = new File(destinationPath);

        try (InputStream in = new FileInputStream(source);
             OutputStream out = new FileOutputStream(destination)) {

            byte[] buf = new byte[1024];
            int length;
            while ((length = in.read(buf)) > 0) {
                out.write(buf, 0, length);
            }
        }
    }

    private void dispatchTakePictureIntent() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            // Ya tienes el permiso, puedes iniciar la actividad de la cámara
            startCameraActivity();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCameraActivity();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, iniciar la actividad de la cámara
                startCameraActivity();
            } else {
                // Permiso denegado, puedes mostrar un mensaje al usuario
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void startCameraActivity() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                File photoFile = createImageFile();
                if (photoFile != null) {
                    Uri photoUri = FileProvider.getUriForFile(this, "com.example.examenpm.fileprovider", photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();

        return image;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Save the photo to the gallery
            galleryAddPic();

            // Set the captured image to the ImageView
            setPic();

        }
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = perfil.getWidth();
        int targetH = perfil.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        perfil.setImageBitmap(bitmap);
    }

    private String compressImage(String imagePath) {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            // Compress the bitmap with quality 75 and write it to the output stream
            bitmap.compress(Bitmap.CompressFormat.WEBP, 20, outputStream);

            // Create a new file for the compressed image
            File compressedFile = new File(getFilesDir(), "compressed_image.webp");
            FileOutputStream fileOutputStream = new FileOutputStream(compressedFile);

            // Write the compressed image data to the file
            fileOutputStream.write(outputStream.toByteArray());
            fileOutputStream.flush();
            fileOutputStream.close();

            return compressedFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return imagePath; // If there is an error, return the original path
        }
    }


    //Funciones de Audio

    private void detenerReproduccion() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            isPlaying = false;
            playButton.setText("Reproducir Audio");
        }
    }

    private void reproducirAudio() {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(archivoSalida);
            mediaPlayer.prepare();
            mediaPlayer.start();
            isPlaying = true;
            playButton.setText("Detener Reproducción");
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al reproducir el audio", Toast.LENGTH_SHORT).show();
        }

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                detenerReproduccion();
            }
        });
    }

    private void detenerGrabacion() {
        if (grabacion != null) {
            grabacion.stop();
            grabacion.release();
            grabacion = null;
        }
    }

    private void iniciarGrabacion() {
        // Asegúrate de detener la reproducción antes de iniciar la grabación
        detenerReproduccion();

        // Verifica si ya hay una grabación en progreso
        if (grabacion == null) {
            grabacion = new MediaRecorder();
            archivoSalida = getExternalCacheDir().getAbsolutePath() + "/audio.3gp";
            grabacion.setAudioSource(MediaRecorder.AudioSource.MIC);
            grabacion.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            grabacion.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            grabacion.setOutputFile(archivoSalida);

            try {
                grabacion.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

            grabacion.start();
            tomarAud.setText("Grabando...");
            Toast.makeText(this, "Grabación iniciada", Toast.LENGTH_SHORT).show();
        } else {
            // Si ya hay una grabación en progreso, detén la grabación
            detenerGrabacion();
            tomarAud.setText("Grabar Audio");
            Toast.makeText(this, "Grabación detenida", Toast.LENGTH_SHORT).show();
        }
    }

    //Funciones para la Base De Datos

    private void Modificar() {
        Intent intent = new Intent(this, Listado.class);
        String numOrden = Orden.getText().toString().trim();
        String periodista = periodist.getText().toString().trim();
        String descrip = Desc.getText().toString().trim();
        String fecha = Fecha.getText().toString().trim();

        // Crea un objeto para almacenar los datos
        String compressedPhotoPath = compressImage(currentPhotoPath);
        Map<String, Object> entrevistaData = new HashMap<>();

        String photoPathInStorage = saveMediaFile(compressedPhotoPath, "images");
        String audioPathInStorage = saveMediaFile(archivoSalida, "audios");

        entrevistaData.put("foto", photoPathInStorage);
        entrevistaData.put("idOrden", numOrden);
        entrevistaData.put("periodista", periodista);
        entrevistaData.put("descripcion", descrip);
        entrevistaData.put("fecha", fecha);
        entrevistaData.put("audio", audioPathInStorage);

        // Obtiene una referencia a la colección "Entrevistas"
        CollectionReference entrevistasCollection = mfirestore.collection("Entrevistas");

        // Realiza una consulta para encontrar el documento con el idOrden específico
        entrevistasCollection.whereEqualTo("idOrden", numOrden)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            // Verifica si se encontraron documentos antes de intentar actualizar
                            if (document.exists()) {
                                // Actualiza el documento con los nuevos datos
                                entrevistasCollection.document(document.getId())
                                        .update(entrevistaData)
                                        .addOnSuccessListener(aVoid -> {
                                            // Éxito al actualizar los datos en la base de datos
                                            Toast.makeText(Modificar.this, "Datos actualizados correctamente", Toast.LENGTH_LONG).show();
                                            startActivity(intent);
                                        })
                                        .addOnFailureListener(e -> {
                                            // Fallo al actualizar los datos en la base de datos
                                            Toast.makeText(Modificar.this, "Error al actualizar datos", Toast.LENGTH_LONG).show();
                                        });
                            } else {
                                // No se encontraron documentos con el idOrden proporcionado
                                Toast.makeText(Modificar.this, "No se encontraron datos para actualizar", Toast.LENGTH_LONG).show();
                            }
                        }
                    } else {
                        // Error al realizar la consulta
                        Toast.makeText(Modificar.this, "Error al obtener datos para actualizar", Toast.LENGTH_LONG).show();
                    }
                });
    }

}






