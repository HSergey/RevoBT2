

package com.revolife.revobt2.revobt;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by admin on 24.08.2016.
 */
public class T9 extends Service {
    final String LOG_TAG = "myLogs";

    private PendingIntent pi;

    // Описание класса слово+его популярность
    private class CWord{
        public String Word;
        public int popularity;
        public CWord(String val){
            String[] arr = val.split(" ");
            this.Word=arr[0];
            this.popularity=Integer.valueOf(arr[1]);
        }
        public CWord(){}
    }


    private ArrayList<CWord> MyDictionary;      // Массива слов => Словарь
    public ArrayList<CWord> MyWords= new ArrayList<>();           // Найденные слова

    public String GoodWord;                 //Наиболее подходящее слово;

    private int p = 0;
    private boolean MyDictionaryIsLoad = false;

    //Добавление словаря W2 к W1
    private void addWords(ArrayList<CWord> W1, ArrayList<CWord> W2 ){
        W1.addAll(W2);
    }

    //Возвращает созданный словарь со словами, ind-ый символ которых совпадает с ch
    private ArrayList<CWord> cutWords(ArrayList<CWord> W1, char ch, int ind){
        ArrayList<CWord> WordsOut = new ArrayList<CWord>();
        for (CWord item: W1)
            if ((item.Word.length()>ind)&&(item.Word.charAt(ind)==ch))
                WordsOut.add(item);
        return WordsOut;
    }

    //Возвращает индекс слова из W1 с критериями самый короткий и самый популярный
    private int MostPopul(ArrayList<CWord> W1){
        int i_very_lit_word,                    // самое короткое слово в текущем поиске для вывода
                lenght_very_lit_word=99999,     // длина
                i_very_popul_word=-1,           // идекс самого короткого и наиболее популярного слова для вывода как наиболее подходящее
                reiting_very_popul_word=0;      // рейтинг
        int i=0;
        for (CWord item: W1){                   // цикл поиска наиболее подходящего слова для вывода
            if (item.Word.length()<lenght_very_lit_word){
                lenght_very_lit_word    = item.Word.length();   // сброс всех параметров на это слово
                i_very_lit_word         = i;
                reiting_very_popul_word = item.popularity;
                i_very_popul_word       = i;
            }
            if ((item.Word.length()==lenght_very_lit_word)&&
                    (reiting_very_popul_word<item.popularity)){ // если нашли более популярное слово среди коротких, то оно наиболее подходит
                reiting_very_popul_word = item.popularity;      // сброс всех параметров
                i_very_popul_word       = i;
            }
            i++;
        }
        return i_very_popul_word;
    }

    //Ищет слова с набором имволов chars
    public String find(String chars){
        if (chars.length()==0) {
            MyWords = new ArrayList<CWord>();
            return GoodWord;
        }
        Log.d(LOG_TAG, "MyService find: набор символов для поиска: "+ chars);
        if (MyWords.size()==0) {                        // Если поиск в первый раз
            Log.d(LOG_TAG, "MyService find: 1 поиск начат");
            p = 0;                                      // будем сравнивать первую букву в слове
            for (int i = 0; i < chars.length(); i++)    // цикл для копирования всех совпадающих слов
                addWords(MyWords, cutWords(MyDictionary, chars.charAt(i), p));  //добавляем слова из MyDictionary в MyWords при совпадении буквы chars[i] с буквой на p месте в слове из MyDictionary
        }else{  //если нажали не первый раз
            p++;
            Log.d(LOG_TAG, "MyService find: "+ p +" поиск начат");
            ArrayList<CWord> Wtmp = new ArrayList<CWord>();
            for (int i = 0; i < chars.length(); i++)    // цикл для копирования всех совпадающих слов
                 addWords(Wtmp,cutWords(MyWords, chars.charAt(i), p)); //добавляем слова из MyWords во временный Wtmp при совпадении буквы chars[i] с буквой на p месте в слове из MyWords
            MyWords=Wtmp;      // Перебиваем новым словарем
        }
        if (MyWords.size()>0)
            GoodWord=MyWords.get(MostPopul(MyWords)).Word;
        else {
            Log.d(LOG_TAG, "MyService find: СЛОВ БОЛЬШЕ НЕТ. Самое подходящее слово: " + GoodWord);
            return "СЛОВО НЕ НАЙДЕНО";
        }
        Log.d(LOG_TAG, "MyService find:"+MyWords.size()+" найдено слов. Самое подходящее слово: "+GoodWord);
        return GoodWord;
    }


    //Фоновая загрузка словаря
    private LoadDictionary ld1;


    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "MyService onCreate");
        //Фоновая загрузка словаря
        ld1 = new LoadDictionary();
        Log.d(LOG_TAG, "MyService Загрузка словаря началась");
        ld1.execute();
    }

    public void onDestroy() {
        Log.d(LOG_TAG, "MyService onDestroy");
        super.onDestroy();
    }



    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "MyService onStartCommand START");
       // PendingIntent pi = intent.getParcelableExtra(MainActivity.PARAM_PINTENT);

        //получаю возможность отвечать SplashActivity через PendingIntent
        // Проверка на ранее запущенный сервис и уже загруженный словарь
        if  ((this.pi = intent.getParcelableExtra(SplashActivity.PARAM_PINTENT))!=null){
            if (MyDictionaryIsLoad) {
                try {
                    Intent int1 = new Intent().putExtra(SplashActivity.PARAM_RESULT, 100);
                    pi.send(T9.this, SplashActivity.STATUS_FINISH, int1);
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }
        }else
            //получаю возможность отвечать MainActivity через PendingIntent
            // Проверка на ранее запущенный сервис и уже загруженный словарь
        if ((this.pi = intent.getParcelableExtra(MainActivity.EWCSSEWW))!=null){
            try {
                String str = intent.getStringExtra(MainActivity.EXTRA_CHARS);

                String str2 =find(str);
                Intent int2 = new Intent().putExtra(MainActivity.PARAM_RESULT, str2);
                pi.send(T9.this, MainActivity.T9_WORD_FOUND, int2);
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }







        return super.onStartCommand(intent, flags, startId);
    }


    void stop() {
        Log.d(LOG_TAG, "MyService stop");
    }

    public IBinder onBind(Intent arg0) {
        return null;
    }

    //Загрузка словаря в фоновом режиме
    class LoadDictionary extends AsyncTask<Integer, Integer, Void> {
        @Override
        protected Void doInBackground(Integer... Void) {
            try {
                Resources r = T9.this.getResources();
                InputStream is = r.openRawResource(R.raw.dictionary2);//R.raw.test);//
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line = reader.readLine();
                int len=Integer.parseInt(line.replaceAll("[\\D]", ""));

                if (len>0) {
                    MyDictionary = new ArrayList<>(len);
                    for (int i=0; i<len;i++) {
                        //SystemClock.sleep(50);
                        MyDictionary.add(new CWord(reader.readLine()));
                        if ((i+1)%(len/50+1) == 0)
                            publishProgress((i+1) * 100 / len);
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... val1) {
            super.onProgressUpdate(val1);
            Log.d(LOG_TAG, "MyService onProgressUpdate: "+ val1[0].toString() +" %");
            if (pi!=null){
                try {
                    Intent int1 = new Intent().putExtra(SplashActivity.PARAM_RESULT, val1[0]);
                    pi.send(T9.this, SplashActivity.STATUS_LOADING, int1);
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }
            //LaunchPb1.setProgress(values[0]);
        }


        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            MyDictionaryIsLoad = true;
            Log.d(LOG_TAG, "MyService onPostExecute: Загрузка словаря 100 %. Всего слов:" + MyDictionary.size() + "Последнее слово: "+MyDictionary.get(MyDictionary.size()-1).Word);
            if (pi!=null){
                try {
                    Intent int1 = new Intent().putExtra(SplashActivity.PARAM_RESULT, 100);
                    pi.send(T9.this, SplashActivity.STATUS_FINISH, int1);
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
