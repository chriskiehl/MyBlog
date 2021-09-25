(ns myblog.admin.util
  (:require [failjure.core :as f])
  (:import (java.time.format DateTimeFormatter)
           (java.time LocalDateTime)
           (java.util Locale)
           (java.time.format TextStyle)
           (java.time.temporal ChronoUnit)))


(def ^:private hour-format (DateTimeFormatter/ofPattern "h:mm a", Locale/ENGLISH))

(def ^:private mdy-format (DateTimeFormatter/ofPattern "M/d/yyyy"))


(defn- weekday-name [date]
  (as-> date $
        (.getDayOfWeek $)
        (.getDisplayName $ TextStyle/FULL Locale/ENGLISH)))


(defn- formattime [^LocalDateTime datetime days]
  (if (< (Math/abs days) 7)
    (format "at %s" (.format datetime hour-format))
    (format (.format datetime mdy-format))))


(defn- plural [amount]
  (if (= amount 1) "" "s"))


(defn- under-month [days months]
  (and (= months 0)
       (> 7 days)))


(defn calendar
  "A clone of moment.js' calendar() function.
  See: https://tinyurl.com/yaoqvmtd"
  [^LocalDateTime date]
  (let [today (java.time.LocalDateTime/now)
        days (.between ChronoUnit/DAYS today date)
        months (Math/abs (.between ChronoUnit/MONTHS today date))
        years (Math/abs (.between ChronoUnit/YEARS today date))]
    (cond
      (= days 0) (format "today %s" (formattime date days))
      (= days 1) (format "tomorrow %s" (formattime date days))
      (= days -1) (format "yesterday %s" (formattime date days))
      (< -7 days 0) (format "last %s %s" (weekday-name date) (formattime date days))
      (> 7 days 0) (format "%s %s" (weekday-name date) (formattime date days))
      (under-month days months) (format "%s days ago" (Math/abs days))
      (> years 0) (format "about %s year%s ago" years (plural years))
      (> months 0) (format "about %s month%s ago" months (plural months))
      :else (formattime date days))))



(defn extension
  "Extract the extension from a filename. myfile.png -> .png"
  [filename]
  (or (re-find #"\.\w+$" filename) ""))


(defn drop-ext
  "Return the filename sans file extension
  foo.jpg -> foo"
  [filename]
  (let [join clojure.string/join
        split clojure.string/split]
    (join "." (butlast (split filename #"\.")))))


(defmacro try*
  "Try macro which executes the body function
  wrapping any errors up in a Failure of ex-info"
  [& body]
  `(try
     ~@body
     (catch Exception e#
       (f/fail (ex-info (.getMessage e#) (Throwable->map e#))))))


(defn short-uuid
  "First 8 chars of a UUID"
  []
  (subs (str (java.util.UUID/randomUUID)) 0 8))



(defn drop-blank-vals
  "Drops any keys from a map which have empty/blank/nil values"
  [coll]
  (reduce-kv
    #(if-not (clojure.string/blank? %3)
       (assoc %1 %2 %3)
       %1)
    {}
    coll))
