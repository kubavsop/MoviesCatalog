package com.example.data.common.mapper

import com.example.data.feature_main_screen.local.entity.MovieElementEntity
import com.example.data.feature_main_screen.local.entity.ReviewShortEntity
import com.example.data.common.dto.MovieElementDto
import com.example.data.common.dto.MoviesPagedListDto
import com.example.data.common.dto.PageInfoDto
import com.example.data.common.dto.ReviewShortDto
import com.example.domain.common.model.MovieElement
import com.example.domain.common.model.MoviesPagedList
import com.example.domain.common.model.PageInfo

fun MoviesPagedListDto.toMoviesPagedList() = MoviesPagedList(
    movies = movies.map { it.toMovieElement() },
    pageInfo = pageInfo.toPageInfo()
)

fun MovieElementEntity.toMovieElement() = MovieElement(
    country = country,
    genres = genres,
    id = id,
    name = name,
    poster = poster,
    averageRating = averageRating,
    year = year,
    userRating = userRating
)

fun MovieElementDto.toMovieElementEntity(currentPage: Int, userRating: Int?) = MovieElementEntity(
    country = country,
    id = id,
    name = name,
    poster = poster,
    year = year,
    page = currentPage,
    genres = genres.map { it.name },
    averageRating =  String.format("%.1f", reviews.map { it.rating }.average()).toDouble(),
    userRating = userRating
)

fun MovieElementDto.toMovieElement() = MovieElement(
    country = country,
    genres = genres.map { it.name },
    id = id,
    name = name,
    poster = poster,
    averageRating = String.format("%.1f", reviews.map { it.rating }.average()).toDouble(),
    year = year,
)

fun PageInfoDto.toPageInfo() = PageInfo(
    currentPage = currentPage,
    pageCount = pageCount,
    pageSize = pageSize
)

fun PageInfo.toPageInfoDto() = PageInfoDto(
    currentPage = currentPage,
    pageCount = pageCount,
    pageSize = pageSize
)
fun ReviewShortDto.toReviewShortEntity() = ReviewShortEntity(
    id = id,
    rating = rating
)