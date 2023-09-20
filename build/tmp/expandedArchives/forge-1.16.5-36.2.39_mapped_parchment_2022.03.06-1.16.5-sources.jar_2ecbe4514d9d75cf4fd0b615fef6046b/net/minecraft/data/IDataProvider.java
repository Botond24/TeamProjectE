package net.minecraft.data;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public interface IDataProvider {
   HashFunction SHA1 = Hashing.sha1();

   /**
    * Performs this provider's action.
    */
   void run(DirectoryCache pCache) throws IOException;

   /**
    * Gets a name for this provider, to use in logging.
    */
   String getName();

   static void save(Gson pGson, DirectoryCache pCache, JsonElement pJsonElement, Path pPath) throws IOException {
      String s = pGson.toJson(pJsonElement);
      String s1 = SHA1.hashUnencodedChars(s).toString();
      if (!Objects.equals(pCache.getHash(pPath), s1) || !Files.exists(pPath)) {
         Files.createDirectories(pPath.getParent());

         try (BufferedWriter bufferedwriter = Files.newBufferedWriter(pPath)) {
            bufferedwriter.write(s);
         }
      }

      pCache.putNew(pPath, s1);
   }
}