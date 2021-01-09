package com.pkpanda.memorygame

import android.animation.ArgbEvaluator
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.jinatonic.confetti.CommonConfetti
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.pkpanda.memorygame.models.BoardSize
import com.pkpanda.memorygame.models.MemoryGame
import com.pkpanda.memorygame.models.UserImageList
import com.pkpanda.memorygame.utiils.*
import com.squareup.picasso.Picasso


class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
    }

    private lateinit var adapter: MemoryBoardAdapter
    private lateinit var memoryGame: MemoryGame
    private lateinit var rvBoard: RecyclerView
    private lateinit var tvMoves: TextView
    private lateinit var tvPairs: TextView
    private lateinit var clRoot: CoordinatorLayout
    private var boardSize: BoardSize = BoardSize.EASY

    private val db = Firebase.firestore
    private var gameName: String? = null
    private var customGameImages: List<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        rvBoard = findViewById(R.id.rvBoard)
        tvMoves = findViewById(R.id.tvMoves)
        tvPairs = findViewById(R.id.tvPairs)
        clRoot = findViewById(R.id.clRoot)
        if (intent.hasExtra(EXTRA_CUSTOM_GAME_NAME)) {
            gameName = intent.getSerializableExtra(EXTRA_CUSTOM_GAME_NAME) as String
        }
        Log.i(TAG, "Game Name = $gameName")
        if (!gameName.isNullOrEmpty()) {
            downloadGame(gameName!!)
        } else {
            setupBoard()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mi_refresh -> {
                if (memoryGame.getNumMoves() > 0 && !memoryGame.haveWonGame()) {
                    showAlertDialog("Quit your current Game?", null, View.OnClickListener {
                        setupBoard()
                    })
                } else {
                    setupBoard()
                }
                return true
            }
        }
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun downloadGame(customGameName: String) {
        db.collection("games").document(customGameName).get().addOnSuccessListener { document ->
            val userImageList = document.toObject(UserImageList::class.java)
            if (userImageList?.images == null) {
                Log.e(TAG, "Invalid custom game data from Firebase")
                Snackbar.make(clRoot,
                    "Sorry, we couldn't find any such game, '$customGameName'",
                    Snackbar.LENGTH_LONG).show()
                return@addOnSuccessListener
            }
            val numCards = userImageList.images.size * 2
            boardSize = BoardSize.getByValue(numCards)
            customGameImages = userImageList.images
            gameName = customGameName

            for (imageUrl in userImageList.images) {
                Picasso.get().load(imageUrl).fetch()
            }
            Snackbar.make(clRoot, "You're now playing '$customGameName'!", Snackbar.LENGTH_LONG)
                .show()
            setupBoard()

        }.addOnFailureListener { exception ->
            Log.e(TAG, "Exception when retrieving game", exception)
        }
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

    private fun setupBoard() {
        supportActionBar?.title = gameName ?: getString(R.string.app_name)
        when (boardSize) {
            BoardSize.EASY -> {
                tvMoves.text = "Easy : 4x2"
                tvPairs.text = "Pairs : 0/4"
            }
            BoardSize.MEDIUM -> {
                tvMoves.text = "Medium : 6x3"
                tvPairs.text = "Pairs : 0/9"
            }
            BoardSize.HARD -> {
                tvMoves.text = "Hard : 6x4"
                tvPairs.text = "Pairs : 0/12"
            }
        }


        memoryGame = MemoryGame(boardSize, customGameImages)
        adapter = MemoryBoardAdapter(
            this,
            boardSize,
            memoryGame.cards,
            object : MemoryBoardAdapter.CardClickListener {
                override fun onCardCLicked(position: Int) {
                    updateGameWithFlip(position)
                }
            })
        rvBoard.adapter = adapter
        rvBoard.setHasFixedSize(true)
        rvBoard.layoutManager = GridLayoutManager(this, boardSize.getWidth())
    }

    private fun updateGameWithFlip(position: Int) {

        if (memoryGame.haveWonGame()) {
            Snackbar.make(clRoot, "You Already won!!", Snackbar.LENGTH_LONG).show()
            return
        }
        if (memoryGame.isCardFaceUp(position)) {
            Snackbar.make(clRoot, "Invalid Move!!", Snackbar.LENGTH_SHORT).show()
            return
        }
        if (memoryGame.flipCard(position)) {
            tvPairs.text = "Pairs : ${memoryGame.numPairsFound} / ${boardSize.getNumPairs()}"
            if (memoryGame.haveWonGame()) {
                Snackbar.make(clRoot, "You Won !!Congrats", Snackbar.LENGTH_LONG).show()
                CommonConfetti.rainingConfetti(
                    clRoot,
                    intArrayOf(Color.YELLOW, Color.MAGENTA, Color.GREEN, Color.RED)
                )
                    .oneShot()
                CommonConfetti.explosion(
                    clRoot,
                    110,
                    100,
                    intArrayOf(Color.YELLOW, Color.MAGENTA, Color.GREEN, Color.RED)
                )
                    .oneShot()
                val handler = Handler()
                handler.postDelayed(Runnable { finish() }, 5000)
            }
        }
        val color = ArgbEvaluator().evaluate(
            memoryGame.numPairsFound.toFloat() / boardSize.getNumPairs(),
            getColor(this, R.color.color_progress_none),
            getColor(this, R.color.color_progress_full)
        ) as Int
        tvPairs.setTextColor(color)
        tvMoves.text = "Moves : ${memoryGame.getNumMoves()}"
        adapter.notifyDataSetChanged()
    }
}