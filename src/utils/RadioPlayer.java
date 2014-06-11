package utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import common.message.MusicGenre;
import model.network.NetworkIOManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhugongpu on 14-6-8.
 */
public class RadioPlayer {

    private static MediaPlayer mediaPlayer = new MediaPlayer();

    /**
     * playList中最多存放的记录数
     */
    private static int MAX_LIST_SIZE = 50;

    /**
     * 存放当前从服务器获取的播放列表
     *
     * @<warnging>不能直接调用，调用时使用getPlayList方法</warnging>
     */
    private static List<Uri> playList = new ArrayList<Uri>();

    private static MusicGenre currentGenre = MusicGenre.ClassicMusic;

    private static int currentSongIndex = 0;

//    private static LoadMusicList loadMusicListTask = null;


    /**
     * 向服务器发送请求，并重置当前播放列表
     *
     * @param genre
     */
    public static void setMusicGenre(MusicGenre genre) throws IOException {

//        //异步请求更新列表
//
//        if (loadMusicListTask != null) {
//            loadMusicListTask.cancel(true);
//            loadMusicListTask = null;
//        }
//
//        loadMusicListTask = new LoadMusicList();
//        loadMusicListTask.execute(genre);

        if (!playList.isEmpty()) {
            playList.clear();
        }

        NetworkIOManager.getInstance().getOutputManager().requestForSong(genre);


    }

    /**
     * 向播放列表中添加一首音乐
     *
     * @param uri 从服务器的到的string类型uri
     */
    public static void addSongToPlayList(String uri) {
        playList.add(Uri.parse(uri));
    }

    /**
     * 根据uri播放对应音乐
     *
     * @param context
     * @param uri
     * @throws IOException
     */
    public static void play(Context context, Uri uri) throws IOException {
        if (mediaPlayer.isPlaying())
            mediaPlayer.stop();

        if (mediaPlayer == null)
            mediaPlayer = new MediaPlayer();

        mediaPlayer.setDataSource(context, uri);
        mediaPlayer.start();
    }

    /**
     * 从列表中任选一首播放
     *
     * @param context
     */
    public static void play(final Context context) throws IOException {
        if (playList.isEmpty()) {
            //以默认类型 向服务器请求列表
            setMusicGenre(currentGenre);
            //todo 等待一段时间，等数据已加载后，重新播放
            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... voids) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    try {
                        play(context);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }


            }.execute();
        } else {
//            //随机播放
//            int index = new Random().nextInt() % playList.size();
            play(context, playList.get(currentSongIndex));
        }
    }


    /**
     * 播放下一首
     *
     * @param context
     */
    public static void playNext(Context context) throws IOException {

        currentSongIndex = (currentSongIndex + 1) % playList.size();

        play(context, playList.get(currentSongIndex));
    }

    /**
     * 播放上一首
     *
     * @param context
     */
    public static void playLast(Context context) throws IOException {

        if (currentSongIndex > 0)
            currentSongIndex--;
        else {
            currentSongIndex = playList.size() - 1;
        }

        play(context, playList.get(currentSongIndex));
    }


    /**
     * 暂停播放
     */
    public static void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying())
            mediaPlayer.pause();
    }


    /**
     * 结束播放,并释放资源
     */
    public static void stop() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }


//    private static class LoadMusicList extends AsyncTask<MusicGenre, Void, Void> {
//
//        @Override
//        protected Void doInBackground(MusicGenre... musicGenres) {
//
//            if (musicGenres.length == 1) {
//                try {
//                    //请求genre类型的列表
//                    NetworkIOManager.getInstance().getOutputManager().requestForSong(musicGenres[0]);
//                    //接受列表，至uri 为 null
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            return null;
//        }
//    }
}
