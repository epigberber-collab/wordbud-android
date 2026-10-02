package app.wordbud.ime;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;

/** Immutable after load. No Android dependencies: tested on the JVM. */
public final class Lexicon {
    public static final class Candidate {
        public final String word, gloss;
        public final int consumed;
        Candidate(String w, String g, int c) { word=w; gloss=g; consumed=c; }
    }
    private final TreeMap<String,List<Candidate>> entries = new TreeMap<>();
    public static Lexicon load(InputStream input) throws IOException {
        Lexicon l = new Lexicon();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new GZIPInputStream(input), StandardCharsets.UTF_8))) {
            String line;
            while ((line=r.readLine())!=null) {
                String[] f=line.split("\t",3);
                if(f.length!=3) continue;
                l.entries.computeIfAbsent(f[0], k->new ArrayList<>()).add(new Candidate(f[1],f[2],f[0].length()));
            }
        }
        return l;
    }
    public List<Candidate> query(String typed) {
        String p=typed.toLowerCase(Locale.ROOT).replace("u:","v");
        List<Candidate> out=new ArrayList<>(); Set<String> seen=new HashSet<>();
        // Exact words first. Then complete initial words so a sentence can be selected in parts.
        for(int n=p.length();n>0;n--) {
            List<Candidate> list=entries.get(p.substring(0,n));
            if(list!=null) for(Candidate c:list) {
                if(seen.add(c.word+"/"+c.consumed)) out.add(c);
                if(out.size()>=36) return out;
            }
        }
        // Only offer incomplete spelling completion if no exact word was found.
        if(out.isEmpty() && p.length()>0) {
            for(Map.Entry<String,List<Candidate>> e:entries.tailMap(p).entrySet()) {
                if(!e.getKey().startsWith(p)) break;
                for(Candidate c:e.getValue()) if(seen.add(c.word)) {
                    out.add(new Candidate(c.word,c.gloss,p.length()));
                    if(out.size()>=24) return out;
                }
            }
        }
        return out;
    }
    public int size() { return entries.values().stream().mapToInt(List::size).sum(); }
}
