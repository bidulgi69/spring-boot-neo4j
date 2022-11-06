package kr.dove.neo4j

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kr.dove.neo4j.api.Article
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class GsonTests {

    object GsonInstance {
        val gson: Gson = GsonBuilder()
            .setLenient()
            .create()
    }

    @Test
    fun serialize_deserialize_list_with_primitive() {
        val list = mutableListOf("a", "b", "c")

        val toJson = GsonInstance.gson.toJson(list)
        val fromJson = GsonInstance.gson.fromJson(toJson, Array<String>::class.java)

        for (i: Int in list.indices) {
            Assertions.assertEquals(list[i], fromJson[i])
        }
    }

    @Test
    fun serialize_deserialize_list_with_data_class() {
        val articles = mutableListOf(
            Article("https://n.news.naver.com/mnews/article/319391284", "Headline 1", "CNN",
                morphs = mutableSetOf<String>().apply {
                add("Trump")
                add("Biden")
                add("Iran")
                add("Ukraine")
                add("WorldCup")
            }),
            Article("https://n.news.naver.com/mnews/article/382193134", "Headline 2", "FOX"),
            Article("https://n.news.naver.com/mnews/article/390854101", "Headline 3", "Washington Post")
        )

        val toJson = GsonInstance.gson.toJson(articles)
        val fromJson = GsonInstance.gson.fromJson(toJson, Array<Article>::class.java)

        for (i: Int in articles.indices) {
            Assertions.assertTrue(articles[i].isEqualTo(fromJson[i]))
        }
    }

    private fun Article.isEqualTo(o: Article): Boolean {
        if (this.morphs.isNotEmpty() && o.morphs.isNotEmpty()
            && this.morphs.size == o.morphs.size) {
            for (morph: String in this.morphs) {
                if (!o.morphs.contains(morph)) return false
            }
        }

        return this.uri == o.uri
                && this.headline == o.headline
                && this.press == o.press
    }
}