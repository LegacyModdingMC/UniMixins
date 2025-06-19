val shadowArtifact: Configuration by configurations.creating
shadowArtifact.isCanBeConsumed = true

artifacts {
    add("shadowArtifact", tasks["shadowJar"]) {
        builtBy(tasks["shadowJar"])
    }
}

unimined.minecraft {

}
