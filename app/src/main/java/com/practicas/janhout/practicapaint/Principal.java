package com.practicas.janhout.practicapaint;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class Principal extends ActionBarActivity {

    private Vista v;
    private AlertDialog alerta;

    private static final int SELECCION_FOTO = 1;

    /*****************************************************************************/
    /***METODOS ON....************************************************************/
    /*****************************************************************************/

   @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECCION_FOTO) {
            if (resultCode == Activity.RESULT_OK) {
                Uri seleccion = data.getData();
                File archivo = new File(getPathFromURI(this, seleccion));
                if (archivo.exists()) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inMutable = true;
                    v.setBitmap(BitmapFactory.decodeFile(archivo.getAbsolutePath(), options));
                }
                v.setFondo(new Canvas(v.getBitmap()));
                v.invalidate();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        v = new Vista(this);
        setContentView(v);
        alerta = null;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_color) {
            dialogoColor(0);
            return true;
        } else if (id == R.id.menu_grosor) {
            selector(getString(R.string.seleccion_grosor));
            return true;
        } else if (id == R.id.menu_nuevo) {
            v = new Vista(this);
            setContentView(v);
            dialogoColor(1);
            return true;
        } else if (id == R.id.menu_cargar) {
            cargarDibujo();
            return true;
        } else if (id == R.id.menu_tipo) {
            selectorTipo();
            return true;
        } else if (id == R.id.menu_deshacer) {
            deshacer();
            return true;
        } else if (id == R.id.menu_guardar) {
            guardarDibujo();
            return true;
        } else if (id == R.id.menu_borrar) {
            v.setTipo(Vista.Dibujo.GOMA);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*****************************************************************************/
    /***IMAGEN A LA GALERIA*******************************************************/
    /*****************************************************************************/

    private void agregarGaleria(File f) {
        Intent intent = new Intent (Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(f);
        intent.setData(uri);
        this.sendBroadcast(intent);
    }

    /*****************************************************************************/
    /***CARGAR DIBUJO*************************************************************/
    /*****************************************************************************/

    private void cargarDibujo() {
        Intent pickMedia = new Intent(Intent.ACTION_PICK);
        pickMedia.setType("image/*");
        startActivityForResult(pickMedia, SELECCION_FOTO);
    }

    /*****************************************************************************/
    /***DESHACER******************************************************************/
    /*****************************************************************************/

    private void deshacer(){
        ArrayMaximo<Bitmap> a = v.getDeshacer();
        if(a.size()>0) {
            Bitmap b = a.sacar();
            v.setDeshacer(a);
            v.setBitmap(b.copy(b.getConfig(), true));
            v.setFondo(new Canvas(v.getBitmap()));
            v.invalidate();
        }
    }

    /*****************************************************************************/
    /***SELECTOR DE COLOR*********************************************************/
    /*****************************************************************************/

    private void dialogoColor(final int evento) {
        int initialColor = v.getColorActual();
        alerta = new ColorPickerDialog(this, initialColor, new ColorPickerDialog.OnColorSelectedListener() {
            @Override
            public void onColorSelected(int color) {
                if (evento == 0) {
                    v.setColorActual(color);
                } else if (evento == 1) {
                    Bitmap b = v.getBitmap();
                    b.eraseColor(color);
                    v.setGoma(color);
                    v.setColorActual(Color.BLACK);
                    v.invalidate();
                }
            }
        });
        alerta.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                alerta.dismiss();
            }
        });
        alerta.show();
    }

    /*****************************************************************************/
    /***PATH FROM URI*************************************************************/
    /*****************************************************************************/

    public String getPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /*****************************************************************************/
    /***GUARDAR DIBUJO************************************************************/
    /*****************************************************************************/

    private void guardarDibujo() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        alert.setTitle(getString(R.string.nombre_guardar));
        final View vista = inflater.inflate(R.layout.guardar_layout, null);
        alert.setView(vista);

        final EditText nomb = (EditText) vista.findViewById(R.id.etFichero);

        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String nombre = nomb.getText().toString();
                if(nombre.equals("")){
                    Toast.makeText(Principal.this, getString(R.string.error_nombre), Toast.LENGTH_SHORT).show();
                }else {
                    File carpeta = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath());
                    File archivo = new File(carpeta, nombre + ".png");
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(archivo);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    v.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, fos);

                    agregarGaleria(archivo);
                }

            }
        });

        alert.setNegativeButton(android.R.string.no, null);
        alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                alerta.dismiss();
            }
        });

        alerta = alert.create();
        alerta.show();
    }

    /*****************************************************************************/
    /***SELECTOR GROSOR***********************************************************/
    /*****************************************************************************/

    private void selector(String dialogo) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        alert.setTitle(dialogo);
        final View vista = inflater.inflate(R.layout.selector_layout, null);
        alert.setView(vista);
        final TextView valor = (TextView) vista.findViewById(R.id.tvSelectorValor);
        final SeekBar barra = (SeekBar) vista.findViewById(R.id.barraSelector);
        final TextView tipo = (TextView) vista.findViewById(R.id.tvSelectorTipo);
        final Paint pincel = v.getPincel();

        barra.setMax(100);
        barra.setProgress((int) pincel.getStrokeWidth());
        tipo.setText(getString(R.string.menu_grosor));

        valor.setText(barra.getProgress() + "");
        barra.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                valor.setText(progress + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                pincel.setStrokeWidth(barra.getProgress());
                v.setPincel(pincel);
            }
        });

        alert.setNegativeButton(android.R.string.no, null);
        alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                alerta.dismiss();
            }
        });

        alerta = alert.create();
        alerta.show();
    }

    /*****************************************************************************/
    /***SELECTOR TIPO DIBUJO******************************************************/
    /*****************************************************************************/

    private void selectorTipo() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        alert.setTitle(getString(R.string.seleccion_tipo));
        final View vista = inflater.inflate(R.layout.tipo_layout, null);
        alert.setView(vista);

        final RadioGroup grupo = (RadioGroup) vista.findViewById(R.id.grupoRadio);

        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                int a = grupo.getCheckedRadioButtonId();
                switch (a) {
                    case R.id.rbCirculo:
                        v.setTipo(Vista.Dibujo.CIRCULO);
                        break;
                    case R.id.rbCirculoR:
                        v.setTipo(Vista.Dibujo.CIRCULO_RELLENO);
                        break;
                    case R.id.rbCuadrado:
                        v.setTipo(Vista.Dibujo.CUADRADO);
                        break;
                    case R.id.rbCuadradoR:
                        v.setTipo(Vista.Dibujo.CUADRADO_RELLENO);
                        break;
                    case R.id.rbRecta:
                        v.setTipo(Vista.Dibujo.RECTA);
                        break;
                    case R.id.rbNormal:
                        v.setTipo(Vista.Dibujo.NORMAL);
                        break;
                    case R.id.rbOval:
                        v.setTipo(Vista.Dibujo.OVAL);
                        break;
                    case R.id.rbOvalR:
                        v.setTipo(Vista.Dibujo.OVAL_RELLENO);
                        break;
                }
            }
        });

        alert.setNegativeButton(android.R.string.no, null);
        alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                alerta.dismiss();
            }
        });

        alerta = alert.create();
        alerta.show();
    }
}
