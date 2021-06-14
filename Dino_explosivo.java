package dinossauro;
import robocode.*;
import java.awt.Color;
import robocode.util.*;
import java.awt.geom.*;


// API help : https://robocode.sourceforge.io/docs/robocode/robocode/Robot.html

/**
 * Dino_explosivo - a robot by DinossauroBebado -  V1.0 - 4/6/21
 */

public class Dino_explosivo extends AdvancedRobot
{
	//variaveis de controle 
	public static double UltEner = 100.0;
	
	public static double UltDist =1000; 
	//variavel de monitoramento
	public static boolean canto  = false; 
	
	//constantes de ajuste 
	public static int TOLERANCIA = 3 ; 
	
	public static int MOV = 50 ; 
	
	//variavel de sentido 
	public static int dir = 1;
	
	private String alvo ; 

	 
 	public void run() {
		//configuracao do robo 
		setColors(Color.blue,Color.blue,Color.white); // body,gun,radar
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
			
		while(true) {
			//radar 
			if(getRadarTurnRemaining()==0.0){

				setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
			}

			execute();

		}
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {

		if (alvo == null ) { //procura alvo 

			alvo = e.getName();

		}

		out.println(">>>>>>>>>>>"+alvo+"<<<<<<<<<<<<<");

		if(e.getName().equals(alvo)){

			//ajuste de variaveis para rumble 
			if(getOthers()>1){
			
				TOLERANCIA = 1 ; 
				
				MOV = 100 ;
			}
			
			radar(e);


			out.println(canto() + "----------- \nX :" + getX()+  "\nY :"+ getY());

			movimento(e);

			mirar(e);

			

		}

	}

	public void onHitWall(HitWallEvent e) {

	 dir *= -1 ;
	   
	}	
	
	public void onHitByBullet(HitByBulletEvent e) {
		
		alvo = e.getName();
	}
	
   public void onHitRobot(HitRobotEvent e	){

	   alvo = e.getName();
   }
	public void onRobotDeath(RobotDeathEvent e){

		if(e.getName().equals(alvo)){

			alvo = null ;
		}
	}

 
 // normaliza um rolamento entre +180 e -180
 double normalizeBearing(double angulo) {
	//decide se eh melhor girar para esquerda ou direita
	while (angulo > 180) {

			angulo -= 360;
	}
	while (angulo < -180) {

			angulo += 360;
	}
	return angulo;
 }


boolean canto(){
	if(getX() > (getBattleFieldWidth()  - (getHeight()*TOLERANCIA))  || getX() < (getHeight()*TOLERANCIA)||
	   getY() > (getBattleFieldHeight() - (getHeight()*TOLERANCIA))  || getY() < (getHeight()*TOLERANCIA)){
		canto = true;
	}
	else {
		canto = false;
	}
	 return canto ;
}

boolean atirou(ScannedRobotEvent e){

	double forcBala = UltEner - e.getEnergy();

	// se a energia do inimigo dinimuiu ele provavelmente atirou 
	if (forcBala< 3.01 && forcBala > 0.09){

		out.println("----------Atirou----------");

		UltEner = e.getEnergy();

		return true ; 
				
	}else{

		UltEner = e.getEnergy();

		return false ;

	}
}

public void radar(ScannedRobotEvent e){
	 
	//radar de lock 
	double anguloParaInimigo = getHeadingRadians() + e.getBearingRadians() ;
		
	double radarTurn = Utils.normalRelativeAngle(anguloParaInimigo - getRadarHeadingRadians());
		
	double extraTurn = Math.min(Math.atan(36.0 / e.getDistance()), Rules.RADAR_TURN_RATE_RADIANS);
		
	radarTurn += (radarTurn< 0 ? - extraTurn : extraTurn);
		
	setTurnRadarRightRadians(radarTurn);

}

public void movimento(ScannedRobotEvent e){


	//muda o circulo para evitar os limites da arena 
	if(canto()){

		setTurnRight(normalizeBearing(e.getBearing())+90);

	}else{

		setTurnRight(normalizeBearing(e.getBearing())-90);

	}


	// detecta tiro 
	double distancia = e.getDistance();

	//no x1 se move quando o inimigo atira
	// no rumble se move constantimente 
	if (atirou(e)){ 
	    if(distancia< UltDist){

			setAhead(3*MOV*dir); //mantem se aproxiamndo do inimigo 

		}else{
			setAhead(MOV*dir);
		}
	}

	
	
	UltDist = distancia;

}
	

public void mirar(ScannedRobotEvent e){

		//previsao de tiro para movimentacao circular 
		//calcula o angulo que o inimigo esta girando e sua velocidade para prever aonde ele estara 
	
	    double forcaTiro = Math.min(3.0,getEnergy());

		double xDino = getX();
		double yDino = getY();

		double bearingAbsoluto = getHeadingRadians() + e.getBearingRadians();

		double inimigoX = getX() + e.getDistance() * Math.sin(bearingAbsoluto);
		double inimigoY = getY() + e.getDistance() * Math.cos(bearingAbsoluto);

		double headingInimigo = e.getHeadingRadians();
		double ultHeadingInimigo = headingInimigo;
		double enemyHeadingChange = headingInimigo - ultHeadingInimigo;
		double velocidadeInimigo = e.getVelocity();
		
		
		double deltaT = 0;
		double battleFieldHeight = getBattleFieldHeight(), battleFieldWidth = getBattleFieldWidth();
		       

		double previsaoX = inimigoX, previsaoY = inimigoY;

		while((++deltaT) * (20.0 - 3.0 * forcaTiro) <  Point2D.Double.distance(xDino, yDino, previsaoX, previsaoY)){
		    		
			previsaoX += Math.sin(headingInimigo) * velocidadeInimigo;
			previsaoY += Math.cos(headingInimigo) * velocidadeInimigo;

			headingInimigo += enemyHeadingChange;

			if(	previsaoX < 18.0  || previsaoY < 18.0 || previsaoX > battleFieldWidth - 18.0|| previsaoY > battleFieldHeight - 18.0){

				previsaoX = Math.min(Math.max(18.0, previsaoX), battleFieldWidth - 18.0);
				    	
				previsaoY = Math.min(Math.max(18.0, previsaoY), battleFieldHeight - 18.0);
				   
				break;
			}
		}
		double theta = Utils.normalAbsoluteAngle(Math.atan2(previsaoX - getX(), previsaoY - getY()));
		    
		
		
		    
		setTurnGunRightRadians(Utils.normalRelativeAngle(theta - getGunHeadingRadians()));
		
		setFire(3);

		}


}
