package platform.client.features.modules.render;
import platform.api.module.Category; import platform.api.module.Module; import platform.api.module.ModuleRegister;
import platform.api.module.setting.*;
import platform.client.utils.render.ColorUtil;
@ModuleRegister(a="AtmoDawnFog",b="Кинематографичная атмосфера: туман, лучи света, заря",c=Category.Render)
public class AtmoDawnFog extends Module{
 public final ModeSetting rezhim=new ModeSetting("Режим","Рассвет","Рассвет","Сумерки","Тема");
 public final SliderSetting plotnost=new SliderSetting("Плотность",0.35F,0.05F,0.8F,0.01F);
 public final SliderSetting vysotaRasseivaniya=new SliderSetting("Высота рассеивания",76.0F,60.0F,120.0F,1.0F);
 public final SliderSetting luchiSveta=new SliderSetting("Лучи света",0.75F,0.0F,1.0F,0.01F);
 public final SliderSetting myagkost=new SliderSetting("Мягкость",0.6F,0.0F,1.0F,0.01F);
 public final BooleanSetting raduga=new BooleanSetting("Радуга",true);
 public final SliderSetting yarkostRadugi=new SliderSetting("Яркость радуги",0.55F,0.1F,1.0F,0.01F);
 public final SliderSetting razmerRadugi=new SliderSetting("Размер радуги",54.0F,46.0F,60.0F,0.5F);
 public final ColorSetting tsvetZari=new ColorSetting("Цвет зари",ColorUtil.a(255,173,122,255));
 public AtmoDawnFog(){ yarkostRadugi.a(()->!raduga.c()); razmerRadugi.a(()->!raduga.c()); tsvetZari.a(()->!rezhim.l("Рассвет")); }
 public static boolean check(){ return false; }
}
