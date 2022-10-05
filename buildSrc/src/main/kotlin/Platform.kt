object Platform {
    object OS {
        val name: String =
            System.getProperty("os.name")

        val arch: String =
            System.getProperty("os.arch")

        val isAppleSilicon: Boolean =
            name == "Mac OS X" && arch == "aarch64"
    }

    val availableProcessors: Int =
        Runtime().availableProcessors()
}

fun Runtime(): Runtime = Runtime.getRuntime()
