"""Convert downloaded CC-CEDICT to a compact offline word/gloss asset (CC BY-SA 4.0)."""
import gzip, re, sys
from pathlib import Path

COMMON = '我 你 他 她 它 的 是 在 了 和 不 有 这 那 吗 啊 好 到 要 就 会 也 很 都 人 上 下 中 大 小 来 去 说 看 想 能 给 对 没 个 一 二 三 今天 明天 昨天 你好 谢谢 再见 学习 开发 编程 语言 中国 输入 输入法 电机 电流 电压 电阻 硬件 软件 可以 什么 为什么 怎么 我们 你们 他们 现在 时间 知道 喜欢 工作 学生 学校 手机 电脑 朋友'.split()
rank = {w:i for i,w in enumerate(COMMON)}
rows={}
pattern = re.compile(r'^\S+ (\S+) \[([^]]+)\] /(.+)/$')
with gzip.open(sys.argv[1], 'rt', encoding='utf-8') as src:
    for line in src:
        m=pattern.match(line.strip())
        if not m: continue
        word, py, gloss=m.groups()
        if not all('\u3400' <= c <= '\u9fff' for c in word) or len(word)>10: continue
        py=re.sub(r'[0-5\s]','',py.lower()).replace('u:','v').replace('ü','v')
        if not re.fullmatch('[a-zv]+',py): continue
        senses=[g for g in gloss.split('/') if g and not g.startswith(('CL:', 'variant of', 'old variant of', 'see ', 'also written ', 'surname '))]
        if not senses: continue
        meaning='; '.join(senses[:2]).replace('\t',' ')[:240]
        key=(py,word)
        if key not in rows: rows[key]=meaning
        elif meaning not in rows[key]: rows[key]=(rows[key]+'; '+meaning)[:240]
ordered=sorted(rows,key=lambda k:(k[0],rank.get(k[1],1000),len(k[1]),k[1]))
out=Path(sys.argv[2]);out.parent.mkdir(parents=True,exist_ok=True)
with gzip.GzipFile(str(out),'wb',mtime=0) as f:
    f.write(''.join(f'{py}\t{w}\t{rows[py,w]}\n' for py,w in ordered).encode())
print(f'{len(ordered)} entries -> {out}')
