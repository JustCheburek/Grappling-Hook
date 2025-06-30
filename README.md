**A Standalone Grappling Hook Plugin!**

![](https://media2.giphy.com/media/31n147ZrLHOdeKW3Gk/giphy.gif?cid=790b761129f43319551ffaed9a5e97bfb1af65ce2d7defe2&rid=giphy.gif&ct=g)

![](https://media1.giphy.com/media/hYy8SLxGCs4y8KhdIq/giphy.gif?cid=790b76119902063a37a4901a29dbf9ab0c5db89b0f002983&rid=giphy.gif&ct=g)

![](https://media1.giphy.com/media/rZjGCONqobo4jhbmr/giphy.gif?cid=790b761104c38b6b3c5dd398cec5d075590bffe1ffe6c336&rid=giphy.gif&ct=g)

![](https://media4.giphy.com/media/ltDO03o1wak7NbQqjM/giphy.gif?cid=790b7611f461771b74cc299d84ac7e010eb7450495608c17&rid=giphy.gif&ct=g)

**Default crafting recipes**
(you can customize any of these in the recipes.yml file)

The plugin now features a progressive crafting system:
1. Wood Hook: Fishing Rod + 8 Wood Planks
2. Stone Hook: Wood Hook + 8 Cobblestone
3. Iron Hook: Stone Hook + 8 Iron Ingots
4. Gold Hook: Iron Hook + 8 Gold Ingots
5. Emerald Hook: Gold Hook + 8 Emeralds
6. Diamond Hook: Emerald Hook + 8 Diamonds

**Features:**
 - No commands necessary.
 - Pull yourself to locations.
 - Pull entities to you.
 - Pull items on the ground to you.
 - Configurable durability on grappling hooks.
 - No permissions required. (But are supported)
 - No configuration required. (But is optional)
 - Plug and Play.
 - Multilanguage support (English, Russian)
 - Progressive crafting system
 - Repair hooks in anvils using crafting materials

**Configuration:**

All grappling hooks and their features can be configured in the recipes.yml file.

Some variable explanations are as follows:
```yaml
velocityThrow - this applies a multiplier on the velocity of the hook when it is cast out (thrown) by the player
velocityPull - this applies a multiplier on the velocity of the player when a hook is pulled in (used) by the player
timeBetweenGrapples - this sets a timer so the hook can only be used every 'x' seconds
fallDamage - if this is false, fall damage will not inflict the user after use
lineBreak - if this is true, the players velocity will be reset when the fishing line snaps
airHook - this allows a hook to be used in the air (like in the last gif)
stickyHook - this allows a hook to stick to walls and ceilings when it hits them
```

**Recipe System**

The plugin now features a progressive crafting system where higher-tier hooks require the previous tier hook as an ingredient. For example:

```yaml
recipe:
  materials:
    W: 'wood_hook'  # This references another hook by its ID
    C: 'COBBLESTONE'
  shape:
    - '[C][C][C]'
    - '[C][W][C]'
    - '[C][C][C]'
```

In this example, the recipe requires a Wood Hook (referenced by its ID 'wood_hook') surrounded by 8 cobblestone blocks.

**Repairing Hooks**

Hooks can be repaired in an anvil using the same material used to craft them:
- Wood Hook: Oak Planks
- Stone Hook: Cobblestone
- Iron Hook: Iron Ingot
- Gold Hook: Gold Ingot
- Emerald Hook: Emerald
- Diamond Hook: Diamond
- Air Hook: Feather
- Water Hook: Water Bucket

Each repair material restores 25% of the hook's maximum uses and costs 1 level of experience.

**Localization**

The plugin supports multiple languages. You can change the language in the config.yml file:
```yaml
# Language setting (en, ru) #
language: 'en'
```

Available languages:
- English (en)
- Russian (ru)

You can add your own language by creating new files in the plugin folder:
- `messages_<language-code>.yml` - for plugin messages
- `recipes_<language-code>.yml` - for item names and descriptions

For recipes localization, you only need to include the name and lore sections for each recipe. For example:
```yaml
recipes:
  1:
    name: '&6Деревянный Крюк'
    lore:
      - '&7Осталось использований - &a[uses]'
  2:
    name: '&6Каменный Крюк'
    lore:
      - '&7Осталось использований - &a[uses]'
```

**Commands**
```yaml
/gh give - puts a grappling hook in the user's hand with 50 uses
/gh give <#> - puts a grappling hook in the user's hand with # of uses
/gh give <player> - adds a grappling hook to the inventory of specified player with 50 uses
/gh give <player> <#> - adds a grappling hook to the inventory of specified player with # of uses
```
**Permissions**
```yaml
grapplinghook.pull.self - Allows player to pull themselves with the hook
grapplinghook.pull.mobs - Allows player to pull mobs with the hook
grapplinghook.pull.players - Allows player to pull other players with the hook
grapplinghook.pull.items - Allows player to pull items with the hook

grapplinghook.craft.1 - Allows player to craft the '1' grappling hook defined in recipes.yml
grapplinghook.craft.2 - Allows player to craft the '2' grappling hook defined in recipes.yml
grapplinghook.craft.3 - Allows player to craft the '3' grappling hook defined in recipes.yml
etc...

grapplinghook.command.give - Allows player to use command /gh give

grapplinghook.player.nopull - Player can not be pulled by other players
grapplinghook.player.nocooldown - Player ignores grapple cooldown (if one is set)
```

Also, if you enjoy my plugins and want to buy me a coffee, [you can donate here](https://www.paypal.com/donate/?cmd=_s-xclick&hosted_button_id=Y47JCZTVLZ7FQ&source=url). Thank you!


