# Forge 1.7.10 example project

This is an example Forge 1.7.10 mod template that can be used to quickstart your modding projects. It comes with [CodeChickenLib](https://github.com/Chicken-Bones/CodeChickenCore) and (optionally) [Mixin](https://github.com/SpongePowered/Mixin) already set up.

## How to use this example project

1. Press the **"Use this template"** button above to create a GitHub project and clone it to your machine. You can also clone this repo, which will keep the template's history. *You shouldn't fork this repo if you're using it to create a mod project, because forked repositories have various disadvantages on GitHub.*
3. Configure the project in `gradle.properties`. You can set whether you want to use mixins or not here.
4. Run `py init_project.py` to generate the project files. This script automatically substitutes your metadata into the source files, and deletes itself and this readme once it's done.
5. Run `./gradlew setupDecompWorkspace eclipse` (you can use `idea` instead of `eclipse`)
6. Now you can open the project in your IDE. Don't forget to edit the mod info in `mcmod.info`. Happy coding!

### Running in Eclipse
ForgeGradle 1.2 doesn't generate launch configurations for Eclipse. To get them, copy the launch configuration of another mod and correct the paths in them. If this is the first mod in your workspace, you can use the [1.12.2 MDK's](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.12.2.html) project as the seed. Download it, run `gradlew eclipse`, and copy the generated `.launch` files into your project. It's convoluted, but it's the easiest way I know.

### Tips

* CodeChickenLib is included in the dependencies, so you can get other non-deobfuscated mods running in your dev environment by putting them either in the `libs` directory you create inside this one (you'll have to run `./gradlew eclipse` (or `idea`)  again whenever you change that directory), or in the `mods` folder of your instance.
* Once you're done, build your project with `./gradlew build`.
* Useful arguments in IDEs:
    * Program arguments to get your mixins working: `--tweakClass org.spongepowered.asm.launch.MixinTweaker --mixin <your.mixin.json>`
    * JVM arguments for if you're writing a coremod: `-Dfml.coreMods.load=<your.coremod.Plugin>`

## Credits
This repo is a fork of https://github.com/anatawa12/ForgeGradle-example which itself is based on the [Forge 1.7.10 MDK](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.7.10.html) -- this repo contains some code from both projects.
