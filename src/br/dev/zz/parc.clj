(ns br.dev.zz.parc
  (:refer-clojure :exclude [find])
  (:require [clojure.java.io :as io]
            [clojure.spec.alpha :as s]
            [clojure.string :as string])
  (:import (java.nio.charset StandardCharsets)
           (java.util Base64)))

(defn parse-netrc-lines
  [rf]
  (let [current-entry (atom nil)]
    (fn
      ([] (rf))
      ([coll]
       (when-let [entry @current-entry]
         (rf coll entry))
       (rf coll))
      ([coll el]
       (cond
         (string/starts-with? el "default")
         (let [kvs (rest (string/split el #"\s+"))
               [old _] (reset-vals! current-entry
                                    (merge {:default true}
                                           (apply hash-map (mapcat (fn [[k v]] [(keyword k) v]) (partition 2 kvs)))))]
           (if old (rf coll old) coll))

         (string/starts-with? el "machine")
         (let [kvs (string/split el #"\s+")
               [old _] (reset-vals! current-entry
                                    (merge {}
                                           (apply hash-map (mapcat (fn [[k v]] [(keyword k) v]) (partition 2 kvs)))))]
           (if old (rf coll old) coll))

         :else
         (let [kvs (string/split (string/triml el) #"\s+")]
           (when (even? (count kvs))
             (swap! current-entry merge (apply hash-map (mapcat (fn [[k v]] [(keyword k) v]) (partition 2 kvs)))))
           coll))))))

(defn parse
  [netrc]
  (with-open [rdr (io/reader netrc)]
    (into [] parse-netrc-lines (line-seq rdr))))

(s/fdef parse
  :args (s/cat :netrc any?)
  :ret (s/coll-of map?))

(defn ->netrc
  [x]
  (if (coll? x)
    x
    (parse x)))

(defn authorization-for
  [{:keys [login password]}]
  (str "Basic "
       (.encodeToString (Base64/getEncoder)
                        (.getBytes (str login ":" password) StandardCharsets/UTF_8))))

(defn find
  [x machine]
  (let [entries (->netrc x)]
    (or (some #(when (= (:machine %) machine) %) entries)
        (some :default entries))))
