package offlineonline.offlineonline.cache;

import com.google.common.collect.Maps;

import java.util.*;
import java.net.InetAddress;

public class ManageCache {

        private LinkedList<List<String>> crackedPlayerCache = new LinkedList<>();


        public void addPlayerCache(List<String> ipUser) {
                if (!this.crackedPlayerCache.contains(ipUser)) {
                        crackedPlayerCache.add(ipUser);
                }
        }

        public void setPlayerCache(LinkedList<List<String>> list){
                crackedPlayerCache = list;
        }

        public boolean contains(String ip, String username) {
                for (List<String> stringList : crackedPlayerCache){
                        if (stringList.contains(ip)&&stringList.contains(username))
                                return true;
                }
                return false;
        }

        public void removePlayerCache(String ip) {
                crackedPlayerCache.removeIf(stringList -> stringList.get(0).equals(ip));
        }

        public LinkedList<List<String>> playerCacheList() {
                return crackedPlayerCache;
        }

}
