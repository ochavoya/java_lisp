(clear)

((lambda (x) (* x x x)) 4)


(defun factorial ( n ) 
    (cond
        (
            ( (or (not (numberp n) ) (< n 0) )    
                                    (write "invalid argument : " n ) )
            ( (or (= n 0) (= n 1))           1                       )
            ( true               
                (let ( (m 2) (result 1))
                    (
                        (while (<= m n)
                            (
                                (setq result (* m result ))
                                (setq m (+ m 1)           )
                            )
                        )
                        result
                    )
                )                     
            )
        )
    )
)


(defun gcd ( a b )
    (
    cond ( 
            ( (and (= a 0) (= b 0)) (write "invalid arguments: " a " , " b )  )
            ( (= b 0)                     a                                   )
            ( (= a 0)                     b                                   )
            ( (< a 0)                    (gcd (- a) b )                       )
            ( (< b 0)                    (gcd (- b) a )                       )
            (true                        ( gcd  b  (% a b) )                  )
        )
    )
)


(defun range (m n )
    (cond 
        (
            (   (= m n )  (push m nil)               )
            (   (> m n)   (range n m)                )
            (   true
                (let ( (start n) (result nil ) )
                     (
                        ( while (<= m start)
                            (
                                (setq result (push start result      ))
                                (setq start (- start 1) )
                            )
                        )
                        result
                     )
                )
            )
        )
    )
)


(defun perfectsquare ( n ) 
    (let ( ( k 1) (sum 0) (value nil) )
        (
            (while (<= sum n)
                (
                (setq value (= sum n)  )
                (setq sum (+ sum k)    )
                (setq k (+ k 2)        )
                )
            )
            value
        )
    )
)


(defun power ( m n ) 
    (cond 
        (
            (  (< n 0)                   (write "Invalid argument (n): " n )  )
            (  ( and (= m 0)  (= n 0) )  1                                    )
            (  (= n 0)                   1                                    )
            (  true                      ( * m ( power m (- n 1 ) ) )         )
        )
    )
) 

(defun square (n) (* n n   ) )

(defun cube   (n) (* n n n ) )

(defun triangular ( n ) (/ (* n (+ n 1) ) 2 ) )


(defun permutations ( m  n ) 
    (cond
        (
            ( (< m 0 ) (write "Invalid arguments: m=" m " and  n=" n )  )
            ( (< m n ) (write "Invalid arguments: m=" m " and  n=" n )  )
            ( (= n 0 )                1                                 )
            ( (= n 1 )                m                                 )
            ( true     (*  m   ( permutations  (- m 1)  (- n 1 ) )   )  )
        )
    )
)

(defun binomial ( m n ) (/  (permutations m n) (factorial n) ) )


