(ns vivid.cmp.leiningen
  (:require
    [leiningen.core.main :as lein]))

(defn- exit
  ([exit-code & msg]
   (when-not (= exit-code 0)
     (throw (ex-info (if (seq msg)
                       (apply print-str msg)
                       "Suppressed exit")
                     {:exit-code exit-code :suppress-msg (empty? msg)}))))
  ([] (exit 0)))

(defn lein-main [cwd args]
  (let [raw-args (re-seq #"[^\s]+" args)]
    (alter-var-root #'lein/*exit-process?* (constantly false))
    (alter-var-root #'lein/*cwd* (constantly cwd))
    (with-redefs [lein/exit exit]
      (apply lein/-main raw-args))))
