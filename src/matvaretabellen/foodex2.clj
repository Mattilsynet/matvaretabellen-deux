(ns matvaretabellen.foodex2)

(defn render-aspect [aspect]
  [:abbr {:title (-> aspect :foodex2/term :foodex2.term/note)}
   (str (-> aspect :foodex2/term :foodex2.term/code)
        " "
        (-> aspect :foodex2/term :foodex2.term/name))])
