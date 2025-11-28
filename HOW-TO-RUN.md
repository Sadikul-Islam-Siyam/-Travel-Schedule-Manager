# How to Run Your Application - Quick Guide

## ✅ RECOMMENDED: Double-Click START.bat

1. **Open File Explorer**
2. **Navigate** to your project folder: `E:\GitHub\temp\-Travel-Schedule-Manager`
3. **Double-click** on `START.bat`
4. **Wait** for the application to launch (may take 10-20 seconds first time)
5. **Enjoy!** Your application is now running

## Alternative: Use Command Line

Open PowerShell or Command Prompt in the project folder and run:

```bash
mvn javafx:run
```

## Important Notes

### ❌ DO NOT Try to Run the JAR Directly
- Don't double-click `.jar` files in the `target` folder
- Don't use `java -jar target/...jar`
- These will give you "Module not found" errors

### ✅ ALWAYS Use One of These Methods:
1. Double-click `START.bat` (easiest)
2. Run `mvn javafx:run` in terminal
3. Use the JavaFX Maven plugin

## Why?

JavaFX applications need special module path configuration. The `START.bat` and Maven plugin handle this automatically for you.

## Troubleshooting

**If START.bat doesn't work:**
1. Check Java is installed: Open CMD and type `java -version`
2. Check Maven is installed: Open CMD and type `mvn -version`
3. If either is missing, download and install them (see README.md)

**If application is slow to start:**
- This is normal on first run
- Maven needs to download dependencies
- Subsequent runs will be much faster

---

## Summary

**To run your application:**
```
Just double-click START.bat
```

That's it! Simple as that. 🎉
