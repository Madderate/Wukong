# Wukong (**悟空** in Chinese)

If you have trouble pinning your custom shortcut to Launcher dynamically, try this library!

## What's this library for?

Sometimes we need to custom some applications' shortcuts dynamically via code (for users, they just
want to follow some steps, and finally pin a cute or beautiful shortcut to Launcher).

But in China, there are so many Launchers, which run on different ROMs, so that we need to adapt
each of them to put our customized shortcuts on every Launchers.

Also, there are some differences between older and newer Android System, which is also annoying,
though there is a Official tool called **ShortcutManagerCompat**. What I want to do is to provide a
common method, so that others can pin their custom shortcuts onto the Launcher, without caring about
the inner implementation, just as the XCompat Classes do.

## How to use it?

It is pretty simple. the whole library just includes a Utility Class called `Wukong` and a Data
Class called `CustomShortcutInfo`.

I recommend you to use `CustomShortcutInfo` to collect the installed application's info, so that you
don't need to do the map things when you try to pin the new shortcut.

### `CustomShortcutInfo`

This Data Class is not that complicated. Its constructor needs at least 5 arguments and at most 7
arguments:

+ `originAppIconDrawable`: The origin app icon's drawable. You can get it
  by `ApplicationInfo.loadIcon(PackageManager)`.
+ `originAppName`: The origin app's name. You can get it
  by `ApplicationInfo.loadLabel(PackageManager)`
+ `packageName`: The app's package name. You can get it by `ApplicationInfo.getPackageName()`
+ `activityPkgName`: The package name of the app's activity which includes `Intent.ACTION_MAIN`
  as `action` and `Intent.CATEGORY_LAUNCHER` as `category`. You can
  use `PackageManager.queryIntentActivities(Intent, Int)` to query all the activities that match.
+ `activityClzName`: Simular as `activityPkgName`, this is also the Class Name of the activity which
  includes `Intent.ACTION_MAIN` as `action` and `Intent.CATEGORY_LAUNCHER` as `category`..
+ `action`: *(Optional) If you don't want to launch the Activity with `Intent.ACTION_MAIN`
  as `action`, set the `action` you want.*
+ `category`: *(Optional) If you don't want to launch the Activity with `Intent.CATEGORY_DEFAULT`
  as `category`, set the `category` you want.*

Certainly, you can set a bitmap you like as your shortcut icon. When your bitmap is ready, just set
it to `customAppIconBmp`;

What's more, if you want to change the shortcut's name, you can also set the name you want
to `customAppName`;

On the devices which OS's version is under Android Oreo, you need to set shortcut `duplicatable`
manually, which is `true` as default;

You can also change the `flags` of the `Intent` if you have needs.

### `Wukong`

This Utility Class now have 2 public methods:

+ `isRequestPinShortcutSupported`: Check whether we can set shortcut dynamically.
+ `requestPinShortcut`: The main method to pin shortcut.

Those are pretty easy methods, I think you can understand how they work with the source code, so I
won't talk too much about this class.

## What's more...

If you find this article via GitHub, you might have seen, or even run the demo application already.
I made a `Compose` project to show how `Wukong` works, and I take some `Compose` and `MVI` practice
in `MainActivity` and `IconSelectActivity`. Hope you can learn something about `Compose` and `MVI`
with these Activities and ViewModels.
