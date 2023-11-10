package com.example.moviescatalog.presentation.feature_film_screen

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import coil.load
import com.example.domain.model.MovieDetails
import com.example.moviescatalog.databinding.FragmentFavoriteBinding
import com.example.moviescatalog.databinding.FragmentFilmBinding
import com.example.moviescatalog.databinding.FragmentMainBinding
import com.example.moviescatalog.presentation.MainActivity
import com.example.moviescatalog.presentation.feature_favorite_screen.FavoriteViewModel
import com.example.moviescatalog.presentation.feature_film_screen.recycler_view.FilmRecyclerViewItem
import com.example.moviescatalog.presentation.feature_film_screen.recycler_view.ReviewListAdapter


class FilmFragment : Fragment() {

    private var _binding: FragmentFilmBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FilmViewModel by activityViewModels()
    private val args: FilmFragmentArgs by navArgs()
    private var fragmentCallBack: FragmentCallBack? = null

    interface FragmentCallBack {
        fun openMainFromFilmScreen()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentCallBack = context as FragmentCallBack
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.state.observe(viewLifecycleOwner, ::handleState)
        binding.movieContent.adapter = ReviewListAdapter()

        viewModel.movieDetails(args.id)
    }

    private fun handleState(state: FilmState) {
        when (state) {
            FilmState.Initial -> Unit
            FilmState.Loading -> showProgress()
            is FilmState.Content -> showContent(state.movieDetails)
        }
    }

    private fun showContent(movieDetails: List<FilmRecyclerViewItem>) {
        with(binding) {
            poster.load((movieDetails[HEADER_INDEX] as  FilmRecyclerViewItem.HeaderItem).poster) {
                crossfade(true)
            }
            (movieContent.adapter as? ReviewListAdapter)?.submitList(movieDetails)
            progressBar.isVisible = false
            movieContent.isVisible = true
            appBarLayout.isVisible = true
        }
    }

    private fun showProgress() {
        with(binding) {
            progressBar.isVisible = true
            movieContent.isVisible = false
            appBarLayout.isVisible = false
        }
    }

    private companion object {
        const val HEADER_INDEX = 0
    }

    override fun onDestroyView() {
        binding.movieContent.adapter = null
        _binding = null
        super.onDestroyView()
    }
}