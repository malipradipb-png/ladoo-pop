# App icon (ladoo) banata hai — GitHub build ke time chalta hai
from PIL import Image, ImageDraw, ImageFilter
import random, os, math
def icon(S):
    im=Image.new('RGBA',(S,S),(0,0,0,0)); d=ImageDraw.Draw(im)
    d.rounded_rectangle([0,0,S-1,S-1],radius=S*0.22,fill=(246,168,33))
    d.rounded_rectangle([S*.05,S*.05,S*.95,S*.95],radius=S*0.19,fill=(42,21,52))
    c=S/2; R=S*0.30
    lad=Image.new('RGBA',(S,S),(0,0,0,0)); ld=ImageDraw.Draw(lad)
    for i in range(40,0,-1):
        t=i/40; r=R*t
        col=(int(214+41*(1-t)), int(106+104*(1-t)), int(12+110*(1-t)))
        ox=-R*.25*(1-t); oy=-R*.3*(1-t)
        ld.ellipse([c+ox-r,c+oy-r+S*.02,c+ox+r,c+oy+r+S*.02],fill=col)
    rnd=random.Random(5)
    for _ in range(45):
        a=rnd.random()*6.28; dd=math.sqrt(rnd.random())*R*.85; rr=S*.014
        x=c+math.cos(a)*dd; y=c+S*.02+math.sin(a)*dd
        ld.ellipse([x-rr,y-rr,x+rr,y+rr],fill=(255,232,160,200) if rnd.random()<.5 else (160,70,0,110))
    sh=Image.new('RGBA',(S,S),(0,0,0,0)); ImageDraw.Draw(sh).ellipse([c-R,c-R+S*.06,c+R,c+R+S*.06],fill=(0,0,0,140))
    im=Image.alpha_composite(im,sh.filter(ImageFilter.GaussianBlur(S*.03))); im=Image.alpha_composite(im,lad)
    g=Image.new('RGBA',(S,S),(0,0,0,0)); ImageDraw.Draw(g).ellipse([c-R*.6,c-R*.58,c-R*.15,c-R*.38],fill=(255,255,255,85))
    return Image.alpha_composite(im,g.filter(ImageFilter.GaussianBlur(S*.01)))
for name,S in [('mdpi',48),('hdpi',72),('xhdpi',96),('xxhdpi',144),('xxxhdpi',192)]:
    os.makedirs(f'app/src/main/res/mipmap-{name}',exist_ok=True)
    icon(S*4).resize((S,S),Image.LANCZOS).save(f'app/src/main/res/mipmap-{name}/ic_launcher.png')
print('icons ok')
