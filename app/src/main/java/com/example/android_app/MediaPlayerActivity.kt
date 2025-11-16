package com.example.android_app

import android.Manifest
import android.media.MediaPlayer
import android.os.*
import android.util.Log
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.net.Uri
import java.io.File

class MediaPlayerActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var seekBar: SeekBar
    private lateinit var volumeSeekBar: SeekBar
    private lateinit var playPauseButton: Button
    private lateinit var nextButton: Button
    private lateinit var prevButton: Button
    private lateinit var shuffleButton: Button
    private lateinit var sortButton: Button
    private lateinit var listView: ListView

    private val handler = Handler(Looper.getMainLooper())
    private val logTag = "MusicPlayer"
    private val musicList = mutableListOf<Pair<String, Uri>>()
    private var currentTrackIndex = 0

    private val updateRunnable = object : Runnable {
        override fun run() {
            if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
                seekBar.progress = mediaPlayer.currentPosition
            }
            handler.postDelayed(this, 500)
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            val granted = results.entries.all { it.value }
            if (granted) {
                Log.d(logTag, "Разрешения предоставлены")
                loadMusicFromStorage()
                updateListView()
            } else {
                Log.d(logTag, "Разрешения не предоставлены")
                Toast.makeText(this, "Please grant permission", Toast.LENGTH_LONG).show()
            }
        }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(logTag, "Приложение выключается")
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
            Log.d(logTag, "MediaPlayer освобожден")
        }
        handler.removeCallbacks(updateRunnable)
        Log.d(logTag, "Handler остановлен")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(logTag, "MediaPlayerActivity создается")
        enableEdgeToEdge()
        setContentView(R.layout.activity_media_player)

        initViews()
        mediaPlayer = MediaPlayer()
        setupListeners()

        requestPermissionLauncher.launch(arrayOf(Manifest.permission.READ_MEDIA_AUDIO))

        setupWindowInsets()
        Log.d(logTag, "MediaPlayerActivity инициализирован")
    }

    private fun initViews() {
        playPauseButton = findViewById(R.id.button3)
        nextButton = findViewById(R.id.button2)
        prevButton = findViewById(R.id.button55)
        shuffleButton = findViewById(R.id.button)
        sortButton = findViewById(R.id.button4)
        seekBar = findViewById(R.id.seekBar3)
        volumeSeekBar = findViewById(R.id.seekBar9)
        listView = findViewById(R.id.listView10)
        Log.d(logTag, "Все View инициализированы")
    }

    private fun setupListeners() {
        setupMediaPlayerListeners()
        setupVolumeControl()
        setupButtonListeners()
        setupListView()
        setupSeekBar()
    }

    private fun setupMediaPlayerListeners() {
        mediaPlayer.setOnCompletionListener {
            Log.d(logTag, "Трек завершен, переключение на следующий")
            if (musicList.isNotEmpty()) {
                currentTrackIndex = (currentTrackIndex + 1) % musicList.size
                playTrack(currentTrackIndex)
            }
        }
    }

    private fun setupVolumeControl() {
        val audioManager = getSystemService(AUDIO_SERVICE) as android.media.AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)
        val currentVolume = audioManager.getStreamVolume(android.media.AudioManager.STREAM_MUSIC)

        volumeSeekBar.max = maxVolume
        volumeSeekBar.progress = currentVolume
        volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    audioManager.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, progress, 0)
                    Log.d(logTag, "Громкость изменена на: $progress/$maxVolume")
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupButtonListeners() {
        sortButton.setOnClickListener {
            if (musicList.isEmpty()) {
                Log.d(logTag, "Попытка сортировки пустого списка треков")
                Toast.makeText(this, "Нет треков для сортировки", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Log.d(logTag, "Сортировка треков по алфавиту")
            musicList.sortBy { it.first.lowercase() }
            currentTrackIndex = 0
            updateListView()
        }

        shuffleButton.setOnClickListener {
            if (musicList.isNotEmpty()) {
                Log.d(logTag, "Перемешивание треков")
                musicList.shuffle()
                currentTrackIndex = 0
                updateListView()
            } else {
                Log.d(logTag, "Попытка перемешивания пустого списка треков")
                Toast.makeText(this, "Нет доступных треков", Toast.LENGTH_SHORT).show()
            }
        }

        playPauseButton.setOnClickListener {
            if (musicList.isEmpty()) {
                Log.d(logTag, "Попытка воспроизведения без треков")
                Toast.makeText(this, "Нет доступных треков", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                playPauseButton.text = "▶"
                Log.d(logTag, "Трек поставлен на паузу: ${musicList[currentTrackIndex].first}")
            } else {
                if (mediaPlayer.currentPosition > 0) {
                    mediaPlayer.start()
                    Log.d(logTag, "Трек возобновлен: ${musicList[currentTrackIndex].first}")
                } else {
                    playTrack(currentTrackIndex)
                }
                playPauseButton.text = "⏸"
            }
        }

        nextButton.setOnClickListener {
            if (musicList.isNotEmpty()) {
                currentTrackIndex = (currentTrackIndex + 1) % musicList.size
                Log.d(logTag, "Переключение на следующий трек: индекс $currentTrackIndex")
                playTrack(currentTrackIndex)
            }
        }

        prevButton.setOnClickListener {
            if (musicList.isNotEmpty()) {
                currentTrackIndex = if (currentTrackIndex - 1 < 0) musicList.size - 1 else currentTrackIndex - 1
                Log.d(logTag, "Переключение на предыдущий трек: индекс $currentTrackIndex")
                playTrack(currentTrackIndex)
            }
        }
    }

    private fun setupListView() {
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mutableListOf())
        listView.adapter = adapter
        listView.setOnItemClickListener { _, _, position, _ ->
            currentTrackIndex = position
            Log.d(logTag, "Выбран трек из списка: индекс $position")
            playTrack(position)
        }
    }

    private fun setupSeekBar() {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && mediaPlayer.isPlaying) {
                    mediaPlayer.seekTo(progress)
                    Log.d(logTag, "Перемотка трека на позицию: $progress мс")
                }
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
    }

    private fun setupWindowInsets() {
        val mainView = findViewById<android.view.View>(R.id.main)
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }
    }

    private fun playTrack(index: Int) {
        if (index !in musicList.indices) return

        val track = musicList[index]
        Log.d(logTag, "Воспроизведение трека: ${track.first}")

        try {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(this, track.second)
            mediaPlayer.prepare()
            mediaPlayer.start()

            seekBar.max = mediaPlayer.duration
            handler.post(updateRunnable)

            playPauseButton.text = "⏸"
            Log.d(logTag, "Трек начал воспроизводиться: ${track.first}")

        } catch (e: Exception) {
            Log.d(logTag, "Ошибка воспроизведения трека: ${e.message}")
            Toast.makeText(this, "Ошибка воспроизведения трека", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadMusicFromStorage() {
        musicList.clear()
        Log.d(logTag, "Начало загрузки треков из хранилища")

        try {
            val musicDir = File(Environment.getExternalStorageDirectory(), "Music")
            Log.d(logTag, "Поиск треков в директории: ${musicDir.absolutePath}")

            if (musicDir.exists() && musicDir.isDirectory) {
                val files = musicDir.listFiles()
                Log.d(logTag, "Найдено файлов в директории: ${files?.size ?: 0}")

                files?.forEach { file ->
                    if (file.isFile && file.name.endsWith(".mp3", true)) {
                        val uri = Uri.fromFile(file)
                        musicList.add(file.name to uri)
                        Log.d(logTag, "Найден трек: ${file.name}")
                    }
                }
            } else {
                Log.d(logTag, "Директория Music не найдена или недоступна")
            }
        } catch (e: Exception) {
            Log.d(logTag, "Ошибка при загрузке музыки: ${e.message}")
        }

        Log.d(logTag, "Загрузка завершена. Найдено треков: ${musicList.size}")
        if (musicList.isNotEmpty()) {
            updateListView()
        }
    }

    private fun updateListView() {
        val adapter = listView.adapter as ArrayAdapter<String>
        adapter.clear()
        adapter.addAll(musicList.map { it.first.substringBeforeLast('.') })
        adapter.notifyDataSetChanged()
        Log.d(logTag, "Список треков обновлен в интерфейсе")
    }

    override fun onPause() {
        super.onPause()
        Log.d(logTag, "Activity приостановлена")
        if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            playPauseButton.text = "▶"
            Log.d(logTag, "Трек приостановлен при уходе из приложения")
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(logTag, "Activity возобновлена")
    }
}