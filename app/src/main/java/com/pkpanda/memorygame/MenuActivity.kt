package com.pkpanda.memorygame

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pkpanda.memorygame.models.BoardSize
import com.pkpanda.memorygame.models.MenuCard
import com.pkpanda.memorygame.utiils.EXTRA_BOARD_SIZE
import com.pkpanda.memorygame.utiils.EXTRA_CUSTOM_GAME_NAME


class MenuActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MenuActivity"
        const val CREATE_REQUEST_CODE = 24
    }

    private lateinit var imgMenu: ImageView
    private lateinit var btnCustomGame: Button
    private lateinit var btnDownloadGame: Button
    private lateinit var rvMenu: RecyclerView
    private lateinit var menuList: MutableList<MenuCard>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        supportActionBar?.hide()


        imgMenu = findViewById(R.id.imgMenu)
        rvMenu = findViewById(R.id.rvMenu)
        btnCustomGame = findViewById(R.id.btnCustomGame)
        btnDownloadGame = findViewById(R.id.btnDownloadGame)

        btnCustomGame.setOnClickListener {
            showGameCreationDialog()
        }

        btnDownloadGame.setOnClickListener {
            showDownloadDialog()
        }

        rvMenu.setHasFixedSize(true)
        menuList = mutableListOf()
        menuList.add(MenuCard(R.drawable.flag, "FLAGS"))
        menuList.add(MenuCard(R.drawable.superhero, "SUPERHEROES"))
        menuList.add(MenuCard(R.drawable.cars, "CARS"))
        menuList.add(MenuCard(R.drawable.sports, "SPORTS"))
        menuList.add(MenuCard(R.drawable.emoji, "EMOJIS"))
        menuList.add(MenuCard(R.drawable.ic_house, "CLIPART"))
        rvMenu.layoutManager = GridLayoutManager(this, 2)
        rvMenu.adapter = MenuAdapter(this, menuList)
    }

    @SuppressLint("InflateParams")
    private fun showGameCreationDialog() {
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size, null)
        val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)
        showAlertDialog("Create New Memory Game", boardSizeView, View.OnClickListener {
            val desiredBoardSize = when (radioGroupSize.checkedRadioButtonId) {
                R.id.rbEasy -> BoardSize.EASY
                R.id.rbMedium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
            val intent = Intent(this, CreateActivity::class.java)
            intent.putExtra(EXTRA_BOARD_SIZE, desiredBoardSize)
            startActivityForResult(intent, CREATE_REQUEST_CODE)
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.i(TAG, "onActivityResult")
        if (requestCode == CREATE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val customGameName = data?.getStringExtra(EXTRA_CUSTOM_GAME_NAME)
            if (customGameName == null) {
                Log.e(MainActivity.TAG, "Error Occurred")
                return
            }
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra(EXTRA_CUSTOM_GAME_NAME, customGameName)
            startActivityForResult(intent, CREATE_REQUEST_CODE)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    @SuppressLint("InflateParams")
    private fun showDownloadDialog() {
        val gameDownloadView =
            LayoutInflater.from(this).inflate(R.layout.dialog_downlaod_game, null)
        showAlertDialog("Fetch Memory Game", gameDownloadView, View.OnClickListener {
            val etDownloadGame = gameDownloadView.findViewById<EditText>(R.id.etDownloadGame)
            val gameToDownload = etDownloadGame.text.toString().trim()
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra(EXTRA_CUSTOM_GAME_NAME, gameToDownload)
            startActivity(intent)
        })
    }

    private fun showAlertDialog(
        title: String,
        view: View?,
        positiveClickListener: View.OnClickListener
    ) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(view)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("OK") { _, _ ->
                positiveClickListener.onClick(null)
            }.show()
    }

}