test=1
defaults={}
defaults['name']='Default Name'
defaults['time']=2
defaults['durability']=0
defaults['amount']=1
defaults['probability']=1.0
defaults['useOnce']=False
defaults['displayName']=None
defaults['lore']=None
defaults['fuelTime']=2
defaults['level']=1
defaults['repairMultiple']=0

import pydot

class CraftedRecipe:
    def __init__(self,identifier,inputs=None,shape=None,output=None):
        self.identifier=identifier
        self.inputs=inputs if inputs!=None else {}
        self.shape=shape if shape!=None else []
        self.output=output if output!=None else []
    def cOutput(self):
        out='\n    '+self.identifier+':'
        out+='\n      inputs:'
        if len(self.shape)==0:
            for input in self.inputs.values():
                out+=input.cOutput('\n        ')
        else:
            for key,input in self.inputs.items():
                out+='\n        '+key+':'
                out+=input.cOutput('\n         ')
            out+='\n      shape:'
            for string in self.shape:
                out+='\n        - '+string
        out+='\n      output:'
        out+=self.output.cOutput('\n        ')
        return out

class Recipe:
    def __init__(self,identifier,name=defaults['name'],time=defaults['time'],inputs=None,upgrades=None,outputs=None,enchantments=None,useOnce=defaults['useOnce'],outputRecipes=None):
        self.identifier=identifier
        self.name=name
        self.time=int(time)
        self.inputs=inputs if inputs!=None else []
        self.upgrades=upgrades if upgrades!=None else []
        self.outputs=outputs if outputs!=None else []
        self.enchantments=enchantments if enchantments!=None else []
        self.outputRecipes=outputRecipes if outputRecipes!=None else []
        self.useOnce=useOnce
        from math import ceil
        self.checkEnchantments()
    def checkEnchantments(self):
        removeEnchantments=[]
        for enchantment in self.enchantments:
            valid=False
            for output in self.outputs:
                for target in enchantment.targets:
                    if target in output.name:
                        valid=True
            if not valid:
                removeEnchantments.append(enchantment)
        for enchantment in removeEnchantments:
            self.enchantments.remove(enchantment)
    def addEnchant(self,enchant):
        self.enchantments.append(enchant)
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
        if len(self.enchantments)>0:
            out+='\n    enchantments:'
            for enchantment in self.enchantments:
                if enchantment.probability!=0:
                    out+=enchantment.cOutput('\n      ') 
        if len(self.outputRecipes)>0:
            out+='\n    output_recipes:'
            for outputRecipe in self.outputRecipes:
                out+='\n      - '+outputRecipe.identifier
        if self.useOnce!=defaults['useOnce']:out+='\n    use_once: '+str(self.useOnce).lower()  
        return out
            
class Enchantment:   
    enchantments={}
    def __init__(self,name=None,type=None,level=defaults['level'],probability=defaults['probability'],targets=[]):    
        self.name=name
        self.type=Enchantment.enchantments[name][0]
        self.level=level
        self.probability=probability
        self.targets=Enchantment.enchantments[name][1]
    @staticmethod
    def importEnchantments(filename='enchantments.csv'):
        import csv
        myfile=open(filename)
        csvReader=csv.reader(myfile)
        for line in csvReader:
            Enchantment.enchantments[line[0]]=(line[1],filter(None,line[2:]))
    def cOutput(self,spacer):
        out=spacer+self.name+' '+str(self.level)+':'
        spacer=spacer+'  '
        out+=spacer+'type: '+self.type
        out+=spacer+'level: '+str(self.level)
        if self.probability!=defaults['probability']: out+=spacer+'probability: '+str(self.probability)
        return out
        
class ItemStack:
    materials={}
    
    def __init__(self,name,material=None,amount=defaults['amount'],durability=None,displayName=defaults['displayName'],lore=defaults['lore']):    
        self.name=name
        self.material=material if material!=None else ItemStack.materials[name][1]
        self.durability=durability if durability!=None else ItemStack.materials[name][3]
        import math 
        self.amount=int(math.ceil(amount))
        self.displayName=displayName
        self.lore=lore
    @staticmethod
    def importMaterials(filename='materials.csv'):
        import csv
        myfile=open(filename)
        csvReader=csv.reader(myfile)
        for line in csvReader:
            commonName,material,id,durability=line
            ItemStack.materials[commonName]=(commonName,material,int(id),int(durability))
    
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
    def equals(self,otherItemStack):
        return self.material==otherItemStack.material and self.durability==otherItemStack.durability and self.amount==otherItemStack.amount and self.displayName==otherItemStack.displayName and self.lore==otherItemStack.lore
ItemStack.importMaterials()
        
defaults['fuel']=ItemStack(name='Charcoal')
defaults['repairInputs']=[ItemStack(name='Charcoal',amount=0)]
class Factory:
    def __init__(self,identifier,name=defaults['name'],fuel=defaults['fuel'],fuelTime=defaults['fuelTime'],inputs=None,outputRecipes=None,repairMultiple=defaults['repairMultiple'],repairInputs=None):
        self.name=name
        self.identifier=identifier
        self.fuel=fuel
        self.fuelTime=int(fuelTime)
        self.inputs=inputs if inputs!=None else []
        self.outputRecipes=outputRecipes if outputRecipes!=None else []
        self.repairMultiple=repairMultiple
        self.repairInputs=repairInputs if repairInputs!=None else defaults['repairInputs']
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
            out+='\n    recipes:'
            for outputRecipe in self.outputRecipes:
                out+='\n      - '+outputRecipe.identifier
        if self.repairMultiple!=defaults['repairMultiple']:out+='\n    repair_multiple: '+str(self.repairMultiple)
        if len(self.repairInputs)>0:
            out+='\n    repair_inputs:'
            for repairInput in self.repairInputs:out+=repairInput.cOutput('\n      ')        
        return out