test=1
defaults={}
defaults['name']='Default Name'
defaults['time']=1
defaults['durability']=0
defaults['amount']=1
defaults['probability']=1.0
defaults['useOnce']=False
defaults['displayName']=None
defaults['lore']=None
defaults['maintenance']=0
defaults['fuelTime']=1
defaults['level']=1

import pydot

class Recipe:
    def __init__(self,identifier,name=defaults['name'],time=defaults['time'],inputs=None,upgrades=None,outputs=None,enchants=None,useOnce=defaults['useOnce'],outputRecipes=None,maintenance=defaults['maintenance']):
        self.identifier=identifier
        self.name=name
        self.time=int(time)
        self.inputs=inputs if inputs!=None else []
        self.upgrades=upgrades if upgrades!=None else []
        self.outputs=outputs if outputs!=None else []
        self.enchants=enchants if enchants!=None else []
        self.outputRecipes=outputRecipes if outputRecipes!=None else []
        self.useOnce=useOnce
        from math import ceil
        self.maintenance=int(ceil(maintenance)) if maintenance!=None else None
    def addEnchant(self,enchant):
        self.enchants.append(enchant)
    def addOutputRecipe(self,recipe):
        self.outputRecipes.append(recipe)
    def cOutput(self):
        out='\n  '+self.identifier+':'
        if self.name!=defaults['name']: out+='\n    name: '+self.name
        if self.time!=defaults['time']: out+='\n    production_time: '+str(self.time)
        if len(self.inputs)>0:
            out+='\n    inputs:'
            for input in self.inputs:out+=input.cOutput('\n      ')
        if len(self.upgrades)>0:
            out+='\n    upgrades:'
            for upgrade in self.upgrades:out+=upgrade.cOutput('\n      ')
        if len(self.outputs)>0:
            out+='\n    outputs:'
            for output in self.outputs:out+=output.cOutput('\n      ')        
        if len(self.enchants)>0:
            out+='\n    enchantments:'
            for enchantment in self.enchants:out+=enchantment.cOutput('\n      ') 
        if len(self.outputRecipes)>0:
            out+='\n    output_recipes:'
            for outputRecipe in self.outputRecipes:
                out+='\n      - '+outputRecipe.identifier
        if self.useOnce!=defaults['useOnce']:out+='\n    use_once: '+str(self.useOnce).lower()  
        if self.maintenance!=None and self.maintenance!=0:out+='\n    maintenance: '+str(self.maintenance)
        return out
    def getShortText(self):
        #spare code: ('' if out=='' else: '\l')+
        out=self.name+'\\n'
        for input in self.inputs:
            out+='I\:'+input.getShortText()+'\l'
        for upgrade in self.upgrades:
            out+='U\:'+upgrade.getShortText()+'\l'
        for output in self.outputs:
            out+='O\:'+output.getShortText()+'\l'
        for enchant in self.enchants:
            if enchant.probability!=0:out+='E\:'+enchant.getShortText()+'\l'
        out+='' if self.maintenance==defaults['maintenance'] else 'M\:'+str(self.maintenance)+'\l'
        return out
    def getNode(self):
        return pydot.Node(name=self.identifier,label=self.getShortText())
    def getEdges(self):
        edges=[]
        for outputRecipe in self.outputRecipes:
            edges.append(pydot.Edge(src=self.identifier,dst=outputRecipe.identifier))
        return edges
            
class Enchant:   
    lookup={'Durability':'DURABILITY','Efficiency':'DIG_SPEED','Silk Touch':'SILK_TOUCH','Fortune':'LOOT_BONUS_BLOCKS'}
    lookup.update({'Sharpness':'DAMAGE_ALL','Bane of the Anthropods':'DAMAGE_ANTHROPODS','Knockback':'KNOCKBACK','Fire Aspect':'FIRE_ASPECT','Looting':'LOOT_BONUS_MOBS'})
    lookup.update({'Power':'ARROW_DAMAGE','Knockback':'ARROW_KNOCKBACK','Flame':'ARROW_FIRE','Infinite':'ARROW_INFINITE'})
    lookup.update({'Fire Protection':'PROTECTION_FIRE','Respiration':'OXYGEN','Protection':'PROTECTION_ENVIRONMENTAL','Projectile Protection':'PROTECTION_PROJECTILE','Feather Falling':'PROTECTION_FALL'})
    def __init__(self,type=None,name=None,level=defaults['level'],probability=defaults['probability']):    
        if type==None and name==None: raise ValueError('Enchantment can\'t lack both a type and name') 
        self.name=type if name==None else name
        self.type=self.lookup[name] if type==None else type        
        self.level=level
        self.probability=probability
    def cOutput(self,spacer):
        out=spacer+self.name+':'
        spacer=spacer+'  '
        out+=spacer+'type: '+self.type
        out+=spacer+'level: '+str(self.level)
        if self.probability!=defaults['probability']: out+=spacer+'probability: '+str(self.probability)
        return out
    def getShortText(self):
        return str(int(self.probability*100))+'% '+self.name+' '+str(self.level)
        

        
class ItemStack:
           
    lookup={'Stone':('STONE',0),'Coal':('COAL',0),'Charcoal':('COAL',1),'Iron Block':('IRON_BLOCK',0),'Gold Block':('GOLD_BLOCK',0),'Diamond Block':('DIAMOND_BLOCK',0),'Leather':('LEATHER',0)}
    lookup.update({'Emerald Block':('EMERALD_BLOCK',0)})
    lookup.update({'Stick':('STICK',0),'Clear Potion':('POTION',7),'Diffuse Potion':('POTION',11),'Artless Potion':('POTION',13),'Thin Potion':('POTION',14),'Bungling Potion':('POTION',23)})
    def __init__(self,material=None,name=None,amount=defaults['amount'],durability=None,displayName=defaults['displayName'],lore=defaults['lore']):    
        if material==None and name==None: raise ValueError('Itemstack can\'t lack both a material and name') 
        self.name=material.replace('_',' ').capitalize() if name==None else name
        self.material=self.lookup[name][0] if material==None else material
        if durability!=None:self.durability=durability
        elif material!=None:self.durability=defaults['durability']
        else: self.durability=self.lookup[name][1]
        import math 
        self.amount=int(math.ceil(amount))
        self.displayName=displayName
        self.lore=lore
    def modifyAmount(self,modifier):
        import copy,math
        copy=copy.copy(self)
        copy.amount=int(math.ceil(copy.amount*modifier))
        return copy
    def cOutput(self,spacer):
        out=spacer+self.name+':'
        spacer=spacer+'  '
        out+=spacer+'material: '+self.material
        if self.amount!=defaults['amount']:out+=spacer+'amount: '+str(self.amount)
        if self.durability!=defaults['durability']:out+=spacer+'durability: '+str(self.durability)
        if self.displayName!=defaults['displayName']:out+=spacer+'display_name: '+self.displayName
        if self.lore!=defaults['lore']:out+=spacer+'lore: '+self.lore
        return out
    def getShortText(self):
        out=str(self.amount)+' '+self.name
        out+='' if self.durability==defaults['durability'] else '('+(str(self.durability))+')' 
        out+='' if self.displayName==defaults['displayName'] else ' '+self.displayName
        out+='' if self.lore==defaults['lore'] else ' '+self.lore
        return out
    
        

defaults['fuel']=ItemStack('COAL',name='Charcoal',durability=1)
defaults['maintenanceInputs']=ItemStack('COAL',name='Charcoal',durability=1)
class Factory:
    def __init__(self,identifier,name=defaults['name'],fuel=defaults['fuel'],fuelTime=defaults['fuelTime'],inputs=None,outputRecipes=None,maintenanceInputs=None):
        self.name=name
        self.identifier=identifier
        self.fuel=fuel
        self.fuelTime=int(fuelTime)
        self.inputs=inputs if inputs!=None else []
        self.outputRecipes=outputRecipes if outputRecipes!=None else []
        self.maintenanceInputs=maintenanceInputs if maintenanceInputs!=None else defaults['maintenanceInputs']
    def addRecipe(self,outputRecipe):
        self.outputRecipes.append(outputRecipe)
    def cOutput(self):
        out='\n  '+self.identifier+':'
        out+='\n    name: '+self.name
        out+='\n    fuel:'+self.fuel.cOutput('\n      ')
        if self.fuelTime!=defaults['fuelTime']:out+='\n    fuel_time: '+str(self.fuelTime)
        out+='\n    inputs:'
        for input in self.inputs:out+=input.cOutput('\n      ')
        if len(self.outputRecipes)>0:
            out+='\n    output_recipes:'
            for outputRecipe in self.outputRecipes:
                out+='\n      - '+outputRecipe.identifier
        if len(self.maintenanceInputs)>0:
            out+='\n    maintenance_inputs:'
            for maintenanceInput in self.maintenanceInputs:out+=maintenanceInput.cOutput('\n      ')        
        return out
    def getShortText(self):
        out=self.name+'\\n'
        for input in self.inputs:
            out+='I\:'+input.getShortText()+'\l'
        out+='F\:'+self.fuel.getShortText()+'\l'
        out+='' if self.fuelTime==defaults['fuelTime'] else 'T\:'+str(self.fuelTime)+'\l'
        for maintenance in self.maintenanceInputs:
            out+='M\:'+maintenance.getShortText()+'\l'
        return out
    def getNode(self):
        return pydot.Node(name=self.identifier,label=self.getShortText())
    def getEdges(self):
        edges=[]
        for recipe in self.outputRecipes:
            edges.append(pydot.Edge(src=self.identifier,dst=recipe.identifier))
        return edges