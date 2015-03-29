% category(X,1) :- projectT(X,_,_,_)... no corresponding elements in rulebase
category(X,2) :- packageT(X,_).
% category(X,?3) :- folder(?X)... no corresponding elements in rulebase
category(X,4) :- fileName(X,N),javaFile(N).
category(X,5) :- fileName(X,N),classFile(N).
% category(X,6) :- package declaration... no corresponding elements in rulebase  
category(X,7) :- importT(X,_,_).
category(X,8) :- initializer(X).
category(X,9) :- class(X).
category(X,10) :- interface(X).
category(X,11) :- enum(X).
category(X,12) :- fieldDefT(X,_,_,_,_).
category(X,13) :- constructor(X).
category(X,14) :- methodDefT(X,_,_,_,_,_,_).
