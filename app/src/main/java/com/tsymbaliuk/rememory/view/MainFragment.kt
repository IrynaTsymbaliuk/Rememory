package com.tsymbaliuk.rememory.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.tsymbaliuk.rememory.R
import com.tsymbaliuk.rememory.model.FirebaseManager
import com.tsymbaliuk.rememory.view.adapter.LevelAdapter
import com.tsymbaliuk.rememory.viewmodel.GameViewModel
import kotlinx.android.synthetic.main.main_fragment.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class MainFragment : Fragment() {

    private val gameViewModel: GameViewModel by sharedViewModel()
    private val firebaseManager: FirebaseManager by inject()
    private lateinit var levelAdapter: LevelAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpLevelRV()

        firebaseManager.getAllLevels().observe(viewLifecycleOwner, Observer { allLevels ->
            levelAdapter.updateData(allLevels)
        })

        gameViewModel.allLevels.observe(viewLifecycleOwner, Observer {
            levelAdapter.updateData(it)
        })

        start_game_button.setOnClickListener(
            Navigation.createNavigateOnClickListener(R.id.action_mainFragment_to_gameFragment)
        )

    }

    private fun setUpLevelRV() {
        levelAdapter = LevelAdapter(requireContext())

        main_rv.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = levelAdapter
        }

        indicator.attachToRecyclerView(main_rv)
    }

}