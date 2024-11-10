package com.example.spotify

import sttp.client4.quick._
import sttp.client4.Response
import sttp.client4.httpurlconnection.HttpURLConnectionBackend
import ujson._

object spotify extends App {
  // Spotify API credentials
  val clientId = "fa89aec1589b4eb8b53c3e92a48c715a"
  val clientSecret = "ca0f7b191208482c99051a5838b03b7d"
  val playlistId = "5Rrf7mqN8uus2AaQQQNdc1"

  def getAccessToken(clientId: String, clientSecret: String): String = {
    val basicAuth = java.util.Base64.getEncoder.encodeToString(s"$clientId:$clientSecret".getBytes)
    val tokenRequest = basicRequest
      .post(uri"https://accounts.spotify.com/api/token")
      .header("Authorization", s"Basic $basicAuth")
      .body(Map("grant_type" -> "client_credentials"))
    val response = tokenRequest.send(backend)
    response.body match {
      case Left(error) => throw new RuntimeException(s"Failed to get token: $error")
      case Right(body) =>
        val json = ujson.read(body)
        json("access_token").str
    }
  }

  // Backend for requests
  implicit val backend = HttpURLConnectionBackend()
  // Get access token
  val accessToken = getAccessToken(clientId, clientSecret)
  // Use the access token to get playlist data
  val playlistRequest = basicRequest
    .get(uri"https://api.spotify.com/v1/playlists/$playlistId")
    .header("Authorization", s"Bearer $accessToken")

  val playlistResponse = playlistRequest.send(backend)

  // Handle response
  playlistResponse.body match {
    case Left(error) => println(s"Error fetching playlist: $error")
    case Right(body) =>
      val playlistData = ujson.read(body)

      // Extract song information: name, duration_ms, and artist IDs
      val songs = playlistData("tracks")("items").arr.map { item =>
        val track = item("track")
        val name = track("name").str
        val durationMs = track("duration_ms").num.toInt
        val artists = track("artists").arr.map(artist => (artist("name").str, artist("id").str))
        (name, durationMs, artists)
      }
      // Sort by duration and take top 10 longest songs
      val top10Songs = songs.sortBy(-_._2).take(10)

      println("Part 1: Top 10 Longest Songs")
      top10Songs.foreach { case (name, duration, _) =>
        println(s"$name, $duration")
      }

      val uniqueArtists = top10Songs.flatMap(_._3).distinct

      val artistFollowerCounts = uniqueArtists.map { case (artistName, artistId) =>
        val artistRequest = basicRequest
          .get(uri"https://api.spotify.com/v1/artists/$artistId")
          .header("Authorization", s"Bearer $accessToken")

        val artistResponse = artistRequest.send(backend)
        artistResponse.body match {
          case Left(error) =>
            println(s"Error fetching artist $artistName: $error")
            (artistName, 0)
          case Right(body) =>
            val artistData = ujson.read(body)
            val followers = artistData("followers")("total").num.toInt
            (artistName, followers)
        }
      }

      println("\nPart 2: Artists Ordered by Follower Count")
      artistFollowerCounts.sortBy(-_._2).foreach { case (name, followers) =>
        println(s"$name: $followers")
      }
  }

  // Close the backend
  backend.close()
}