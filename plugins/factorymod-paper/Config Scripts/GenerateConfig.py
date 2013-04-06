from ConfigObjects import Recipe, Enchant, ItemStack, Factory
from ParseConfig import ParseConfig

import pydot
gMod=1

def main():
    print 'Running....'
    config=ParseConfig.parseConfig('..\FactoryMod\src\config.yml')
    config=createTools()
    ParseConfig.saveConfig(config)
    ParseConfig.saveGraph(config)
def createConfig():
    trees=['Food','Armory','Tools','Enchanting','Metals']
    configs={}
    configs['Food']=createFood()
    configs['Cauldron']=createCauldron()
    configs['Armory']=createArmory()
    configs['Tools']=createTools()
    configs['Enchanting']=createEnchanting()
    configs['Metals']=createMetalTech()
    recipes=[]
    factories=[]
    for config in configs.values():
        recipes=recipes+config['recipes']
        factories=factories+config['factories']
    config={'recipes':recipes,'factories':factories}
    ParseConfig.saveConfig('my_config.yml',config)
    
    graph=pydot.Graph("Main")
    graph.set('rankdir','LR')
    
    foodSubgraphs={}
    foodNames=['Food','Enchanting','Cauldron']
    for name in foodNames:
        foodSubgraphs[name]=createGraph('cluster_'+name,configs[name])
    for subgraph in foodSubgraphs.values():
        subgraph.set('fontsize','24')
    foodSubgraphs['Food'].set('label','<<B>Food Tree</B>>')
    foodSubgraphs['Enchanting'].set('label','<<B>Enchanting Tree</B>>')
    foodSubgraphs['Cauldron'].set('label','<<B>Enchanting Resource Tree</B>>')
    graphFood=pydot.Subgraph('cluster_Food')
    graphFood.set('label','<<B>Food Based Tech Trees</B>>')
    graphFood.set('fontsize','24')
    graphFood.add_subgraph(foodSubgraphs['Food'])
    graphFood.add_subgraph(foodSubgraphs['Enchanting'])
    graphFood.add_subgraph(foodSubgraphs['Cauldron'])
    graph.add_subgraph(graphFood)
    
    metalSubgraphs={}
    metalNames=['Armory','Tools','Metals']
    for name in metalNames:
        metalSubgraphs[name]=createGraph('cluster_'+name,configs[name])
    for subgraph in metalSubgraphs.values():
        subgraph.set('fontsize','24')
    metalSubgraphs['Armory'].set('label','<<B>Armor Tree</B>>')
    metalSubgraphs['Tools'].set('label','<<B>Tools and Weapons Tree</B>>')
    metalSubgraphs['Metals'].set('label','<<B>Mining Research Tree</B>>')
    graphMetal=pydot.Subgraph('cluster_Metal')
    graphMetal.set('label','<<B>Mining Based Tech Trees</B>>')
    graphMetal.set('fontsize','24')
    graphMetal.add_subgraph(metalSubgraphs['Armory'])
    graphMetal.add_subgraph(metalSubgraphs['Metals'])
    graphMetal.add_subgraph(metalSubgraphs['Tools'])
    graphMetal.set('rankdir','LR')
    graph.add_subgraph(graphMetal)
    
    filename='graph.gv'
    file=open(filename,'w')
    file.write(graph.to_string())  

def createTools():
    factories={}
    recipes={}
    tools=['Sword','Axe','Pickaxe','Spade']#Damage goes 6,5,4,3
    techs=['Stone','Iron','Gold','Diamond']
    resources={'Stone':'Smooth Stone','Iron':'Iron Block','Gold':'Gold Block','Diamond':'Diamond Block'}
    modTools={'Sword':1,'Axe':.5,'Pickaxe':.75,'Spade':.25}
    modTechs={'Stone':1,'Iron':1,'Gold':1,'Diamond':1}
    modTree=8
    modGlobal=1
    quantEff=10
    modEff=.7
    modUpg=4
    for tech in reversed(techs):
        for tool in tools:
            #E Factory
            inputs=[ItemStack(name=resources[tech],amount=modGlobal*modTree*modTechs[tech]*modTools[tool]*modEff*quantEff)]
            outputs=[ItemStack(material=tech.upper()+'_'+tool.upper(),name=tech+' '+tool,amount=quantEff)]
            recipes['E'+tech+tool]=Recipe('E'+tech+tool,name='Forge '+tech+' '+tool+'s efficiently',inputs=inputs,outputs=outputs)
            #U E Factory
            inputs=[ItemStack(name='Coal',amount=modGlobal*modTree*modTechs[tech]*modTools[tool]*modUpg*modUpg)]
            outputRecipes=[recipes['E'+tech+tool]]
            recipes['UE'+tech+tool]=Recipe('UE'+tech+tool,name='Upgrade to efficient '+tech+' '+tool+' production',inputs=inputs,outputRecipes=outputRecipes)
            #Factory
            inputs=[ItemStack(name=resources[tech],amount=modGlobal*modTree*modTechs[tech]*modTools[tool])]
            outputs=[ItemStack(material=tech.upper()+'_'+tool.upper(),name=tech+' '+tool)]
            recipes[tech+tool]=Recipe(tech+tool,name='Forge '+tech+' '+tool,inputs=inputs,outputs=outputs)
            #U Factory
            inputs=[ItemStack(name='Coal',amount=modGlobal*modTree*modTechs[tech]*modTools[tool]*modUpg*modUpg)]
            #Upgraded Factory for tool, upgrade to the next tools factory, upgrade to a more efficient tool factory
            outputRecipes=[recipes[tech+tool],recipes['UE'+tech+tool]]
            if tech!='Diamond':
                outputRecipes+=[recipes['U'+techs[techs.index(tech)+1]+tool]]
            recipes['U'+tech+tool]=Recipe('U'+tech+tool,name='Upgrade to '+tech+' '+tool,inputs=inputs,outputRecipes=outputRecipes)
    for tool in tools:
        factories[tool+"SMITHY"]=Factory(tool+"SMITHY",name=tool+' Smithy',fuel=ItemStack(name='Charcoal'),inputs=[ItemStack(name='Coal',amount=tools.index(tool)+1)])
        factories[tool+"SMITHY"].addRecipe(recipes['U'+tech+tool])
    return {'factories':factories,'recipes':recipes}
            
    
def createFood():
    factories={}
    recipes={}
    #def __init__(self,name,number=0,time=defaultTime,outputs=[],inputs=[],enchants=[],useOnce=defaultUseOnce,outputRecipes=[]):
    recipes['Golden Apple']=Recipe('Golden Apple',inputs=[ItemStack('APPLE'),ItemStack('GOLD_INGOT')],outputs=[ItemStack('GOLDEN_APPLE')])
    recipes['E Golden Apple']=Recipe('Efficient Golden Apple',inputs=[ItemStack('APPLE'),ItemStack('GOLD_INGOT')],outputs=[ItemStack('GOLDEN_APPLE')])
    recipes['Golden Carrot']=Recipe('Golden Carrot',inputs=[ItemStack('CARROT'),ItemStack('GOLD_INGOT')],outputs=[ItemStack('GOLDEN_CARROT')])
    recipes['E Golden Carrot']=Recipe('Efficient Golden Carrot',inputs=[ItemStack('CARROT'),ItemStack('GOLD_INGOT')],outputs=[ItemStack('GOLDEN_CARROT')])
    recipes['Beef']=Recipe('Beef',inputs=[ItemStack('RAW_BEEF')],outputs=[ItemStack('COOKED_BEEF')])
    recipes['E Beef']=Recipe('Efficient Beef',inputs=[ItemStack('RAW_BEEF')],outputs=[ItemStack('COOKED_BEEF')])
    recipes['Cake']=Recipe('Cake',inputs=[ItemStack('WHEAT'),ItemStack('SUGAR'),ItemStack('EGGS')],outputs=[ItemStack('CAKE')])
    recipes['E Cake']=Recipe('Efficient Cake',inputs=[ItemStack('WHEAT'),ItemStack('SUGAR'),ItemStack('EGGS')],outputs=[ItemStack('CAKE')])
    recipes['Pumpkin Pie']=Recipe('Pumpkin Pit',inputs=[ItemStack('PUMPKIN'),ItemStack('SUGAR'),ItemStack('EGGS')],outputs=[ItemStack('PUMPKIN_PIE')])
    recipes['E Pumpkin Pie']=Recipe('Efficient Pumpkin Pit',inputs=[ItemStack('PUMPKIN'),ItemStack('SUGAR'),ItemStack('EGGS')],outputs=[ItemStack('PUMPKIN_PIE')])
    recipes['Pork']=Recipe('Pork',inputs=[ItemStack('PORK')],outputs=[ItemStack('GRILLED_PORK')])
    recipes['E Pork']=Recipe('Efficient Pork',inputs=[ItemStack('PORK')],outputs=[ItemStack('GRILLED_PORK')])
    recipes['Chicken']=Recipe('Chicken',inputs=[ItemStack('RAW_CHICKEN')],outputs=[ItemStack('COOKED_CHICKEN')])
    recipes['E Chicken']=Recipe('Efficient Chicken',inputs=[ItemStack('RAW_CHICKEN')],outputs=[ItemStack('COOKED_CHICKEN')])
    recipes['Baked Potato']=Recipe('Baked Potato',inputs=[ItemStack('POTATO')],outputs=[ItemStack('BAKED_POTATO')])
    recipes['E Baked Potato']=Recipe('Efficient Baked Potato',inputs=[ItemStack('POTATO')],outputs=[ItemStack('BAKED_POTATO')])
    recipes['Bread']=Recipe('Bread',inputs=[ItemStack('WHEAT'),ItemStack('SUGAR')],outputs=[ItemStack('BREAD')])
    recipes['E Bread']=Recipe('Efficient Bread',inputs=[ItemStack('WHEAT'),ItemStack('SUGAR')],outputs=[ItemStack('BREAD')])
    recipes['Cookie']=Recipe('Cookies!',inputs=[ItemStack('COCOA'),ItemStack('WHEAT')],outputs=[ItemStack('COOKIE')])
    recipes['E Cookie']=Recipe('Efficient Cookies!',inputs=[ItemStack('COCOA'),ItemStack('WHEAT')],outputs=[ItemStack('COOKIE')])
    recipes['Mushroom Soup']=Recipe('Mushroom Soup',inputs=[ItemStack('BROWN_MUSHROOM'),ItemStack('RED_MUSHROOM')],outputs=[ItemStack('MUSHROOM_STEW')])
    recipes['E Mushroom Soup']=Recipe('Efficient Mushroom Soup',inputs=[ItemStack('BROWN_MUSHROOM'),ItemStack('RED_MUSHROOM')],outputs=[ItemStack('MUSHROOM_STEW')])
    
    recipes['U E Beef']=Recipe('Upgrade to Efficient Beef',outputRecipes=[recipes['E Beef']])
    recipes['U E Golden Carrot']=Recipe('Upgrade to Efficient Golden Carrots',outputRecipes=[recipes['E Golden Carrot']])
    recipes['U Golden Carrot and Beef']=Recipe('Upgrade to Golden Carrots',outputRecipes=[recipes['Golden Carrot'],recipes['U E Golden Carrot'],recipes['Beef'],recipes['U E Beef']])
    recipes['U E Golden Apple']=Recipe('Upgrade to Efficient Golden Apples',outputRecipes=[recipes['E Golden Apple']])
    recipes['U Golden Apple']=Recipe('Upgrade to Golden Apples',outputRecipes=[recipes['Golden Apple'],recipes['U E Golden Apple']])
    recipes['U E Cake']=Recipe('Upgrade to Efficient Cake',outputRecipes=[recipes['E Cake']])
    recipes['U E Chicken']=Recipe('Upgrade to Efficient Chicken',outputRecipes=[recipes['E Chicken']])
    recipes['U Cake and Chicken']=Recipe('Upgrade to Cake and Chicken',outputRecipes=[recipes['Cake'],recipes['U E Cake'],recipes['Chicken'],recipes['U E Chicken'],recipes['U Golden Apple']])
    recipes['U E Pumpkin Pie']=Recipe('Upgrade to Efficient Pumpkin Pie',outputRecipes=[recipes['E Pumpkin Pie']])
    recipes['U E Pork']=Recipe('Upgrade to Efficient Pork',outputRecipes=[recipes['E Pork']])
    recipes['U Pumpkin Pie and Pork']=Recipe('Upgrade to Pumpkin Pie',outputRecipes=[recipes['Pumpkin Pie'],recipes['U E Pumpkin Pie'],recipes['Pork'],recipes['U E Pork'],recipes['U Golden Carrot and Beef']])
    recipes['U E Bread']=Recipe('Upgrade to Efficient Bread',outputRecipes=[recipes['E Bread']])
    recipes['U E Baked Potato']=Recipe('Upgrade to Efficient Baked Potato',outputRecipes=[recipes['E Baked Potato']])
    recipes['U E Cookie']=Recipe('Upgrade to Efficient Cookie',outputRecipes=[recipes['E Cookie']])
    recipes['U E Mushroom Soup']=Recipe('Upgrade to Efficient Mushroom Soup',outputRecipes=[recipes['E Mushroom Soup']])
    recipes['U Bread and Cookies']=Recipe('Upgrade to Bread and Cookies!',outputRecipes=[recipes['Bread'],recipes['U E Bread'],recipes['Cookie'],recipes['U E Cookie'],recipes['U Cake and Chicken']])
    recipes['U Potato and Mushroom Soup']=Recipe('Upgrade to Potato and Mushroom Soup',outputRecipes=[recipes['Baked Potato'],recipes['U E Baked Potato'],recipes['Mushroom Soup'],recipes['U E Mushroom Soup'],recipes['U Pumpkin Pie and Pork']])
    
    factories['Bakery']=Factory('Bakery','BAKERY','COAL',fuelData=1)
    factories['Bakery'].addRecipe(recipes['U Bread and Cookies'])
    factories['Bakery'].addRecipe(recipes['U Potato and Mushroom Soup'])
    
    return {'factories':factories.values(),'recipes':recipes.values()}
    
def createArmory():
    factories={}
    recipes={}
    aMod=2#modifier for armor recipes
    pMod=2#Modifier for production recipes
    eMod=5#Modified for efficient upgrade
    eff=.7#Decrease in cost of efficient plant
    bulk=10#Bulk amount produced by efficient plant
    costs={'Helmet':5,'Chestplate':8,'Leggings':7,'Boots':4}# Modifier for different branches of the tree, based on vanilla costs
    techCosts={'Leather':1,'Iron':2,'Gold':4,'Diamond':6}#Modified for the different tiers of the tree
    factories['Armory']=Factory("Armory","ARMORY","COAL",fuelData=1,mats=[ItemStack("COAL",amount=10)])
    techs=['Leather','Iron','Gold','Diamond']
    techMat={'Leather':'LEATHER','Iron':'IRON_BLOCK','Gold':'GOLD_BLOCK','Diamond':'DIAMOND_BLOCK'}
    items=['Helmet','Chestplate','Leggings','Boots']

    for i in reversed(range(len(techs))):
        tech=techs[i]
        for item in items:
            #Standard Production Recipe
            inputs0=[ItemStack(techMat[tech],amount=gMod*aMod*pMod*costs[item])]
            outputs0=[ItemStack(tech.upper()+' '+item.upper())]
            recipes[tech+' '+item]=Recipe(tech+' '+item,inputs=inputs0,outputs=outputs0)
            #Efficient Production Recipe
            inputs1=[ItemStack(techMat[tech],amount=gMod*aMod*pMod*costs[item]*bulk*eff)]
            outputs1=[ItemStack(tech.upper()+' '+item.upper(),amount=bulk)]
            recipes['E '+tech+' '+item]=Recipe('Efficient '+tech+' '+item,inputs=inputs1,outputs=outputs1)
            #Upgrade to Efficient Production
            inputs2=[ItemStack('COAL',amount=gMod*aMod*eMod*costs[item]*techCosts[tech])]
            recipes['U E '+tech+' '+item]=Recipe('Upgrade to Efficent '+tech+' '+item,inputs=inputs2,outputRecipes=[recipes['E '+tech+' '+item]])
            #Upgrade to Standard Production
            inputs3=[ItemStack('COAL',amount=gMod*aMod*costs[item]*techCosts[tech])]
            outputRecipes=[recipes[tech+' '+item],recipes['U E '+tech+' '+item],recipes['U E '+tech+' '+item]]
            if tech!='Diamond':
                outputRecipes.append(recipes['U '+techs[i+1]+' '+item])
            recipes['U '+tech+' '+item]=Recipe('Upgrade to '+tech+' '+item,inputs=inputs3,outputRecipes=outputRecipes)
    for item in items:
        factories['Armory'].addRecipe(recipes['U Leather '+item])
    return {'factories':factories.values(),'recipes':recipes.values()}
    
def createEnchanting():
    factories={}
    recipes={}
    inputs=[7,11,13,14,23]
    
    branches={'Industrial','Offensive','Defensive'}
    #Structure:Dict{branch:Dict{enchantment:Dict{level:[teir0Prob,tier1Prob,...}}
    tiers=5
    enchantments={}
    items={}
    
    #enchantments['Industrial']={'DURABILITY','DIG_SPEED','SILK_TOUCH','LOOT_BONUS_BLOCKS'}
    enchantments['Industrial']={}
    items['Industrial']=['STONE_PICKAXE','IRON_PICKAXE','GOLD_PICKAXE','DIAMOND_PICKAXE','STONE_AXE','IRON_AXE','GOLD_AXE','DIAMOND_AXE','STONE_SPADE','IRON_SPADE','GOLD_PICKAXE','DIAMOND_PICKAXE']
    enchantments['Industrial']['DURABILITY']={1:[.5,.6,.7,.8,.9],2:[0,.2,.4,.6,.8],3:[0,0,0,.1,.3]}
    enchantments['Industrial']['SILK_TOUCH']={1:[.5,.4,.4,0,0]}
    enchantments['Industrial']['DIG_SPEED']={1:[.5,.5,.7,.8,.9],2:[.2,.3,.4,.5,.6,.7],3:[0,.1,.2,.3,.4,.5],4:[0,0,.1,.2,.3],5:[0,0,0,0,.1]}
    enchantments['Industrial']['LOOT_BONUS_BLOCKS']={1:[0,.3,.5,.5,.5],2:[0,0,.3,.5,.5],3:[0,0,0,0,.1]}
        
    #enchantments['Melee']={'DAMAGE_ALL','DAMAGE_ANTHROPODS','KNOCKBACK','FIRE_ASPECT','LOOT_BONUS_MOBS'}
    enchantments['Melee']={}
    items['Melee']={'STONE_AXE','IRON_AXE','GOLD_AXE','DIAMOND_AXE','STONE_SWORD','IRON_SWORD','GOLD_SWORD','DIAMOND_SWORD'}
    enchantments['Melee']['DAMAGE_ALL']={1:[.7,.5,.3,0,0],2:[.2,.4,.5,.3,.1],3:[0,0,.3,.5,.2],4:[0,0,0,.3,.3],5:[0,0,0,0,.1]}
    enchantments['Melee']['DAMAGE_ANTHROPODS']={1:[.1,.1,.1,.1,.1]}
    enchantments['Melee']['KNOCKBACK']={1:[.5,.5,.5,.3,.2],2:[0,0,.4,.5,.2]}
    enchantments['Melee']['FIRE_ASPECT']={1:[.2,.3,.4,.4,.3],2:[0,0,0,.1,.2]}
    enchantments['Melee']['LOOT_BONUS_MOBS']={1:[.8,.8,.5,0,0],2:[.2,.5,.7,.4,.2],3:[0,0,0,.3,.4]}
    
    #enchantments['Bows']={'ARROW_DAMAGE','ARROW_KNOCKBACK','ARROW_FIRE','ARROW_INFINITE'}
    enchantments['Bows']={}
    items['Bows']={'BOW'}
    enchantments['Bows']['ARROW_DAMAGE']={1:[.3,.4,.5,.4,.3],2:[.4,.5,.5,.3,.2],3:[0,.2,.3,.4,.2],4:[0,0,0,.2,.2],5:[0,0,0,0,.1]}
    enchantments['Bows']['ARROW_KNOCKBACK']={1:[.1,.2,.3,.4,.4],2:[0,0,0,.1,.2]}
    enchantments['Bows']['ARROW_FIRE']={1:[0,0,0,.1,.2]}
    enchantments['Bows']['ARROW_INFINITE']={1:[.9,.7,.5,.3,.2]}
    #enchantments['Defensive']={'PROTECTION_FIRE','OXYGEN','PROTECTION_ENVIRONMENTAL','PROTECTION_PROJECTILE','PROTECTION FALL'}

    
    
    factories['Enchanting Table']=Factory('Enchanting Table','ENCHANTING','COAL',fuelData=1)
    
    for branch in enchantments.keys():#Go through different classes of enchanments
        for tier in reversed(range(tiers)):#Goes thougheach tier of more advanced enchantments
            recipes[branch+' '+str(tier)]=Recipe('Level '+str(tier+1)+' '+branch+' Enchantments',inputs=[ItemStack('POTION',data=inputs[tier])])
            for enchantment in enchantments[branch].keys():#Goes through different echantments that can be applied to this class
                for level in enchantments[branch][enchantment].keys():#Goes through the different levels that can be applied to that enchant
                    recipes[branch+' '+str(tier)].addEnchant(Enchant(enchantment,level,enchantments[branch][enchantment][level][tier]))
            outputRecipes=[recipes[branch+' '+str(tier)]]
            if tier!=tiers-1:
                outputRecipes.append(recipes['U '+branch+' '+str(tier+1)])
            recipes['U '+branch+' '+str(tier)]=Recipe('Upgrade to Level '+str(tier+1)+' '+branch+' Enchantments',inputs=[ItemStack('POTION',data=inputs[tier])],outputRecipes=outputRecipes)
        factories['Enchanting Table'].addRecipe(recipes['U '+branch+' 0'])

    return {'factories':factories.values(),'recipes':recipes.values()}

def createMetalTech():
    factories={}
    recipes={}
    techLevels=['Stone','Iron','Gold','Diamond','Emerald']
    techInputs={'Stone':'COBBLE','Iron':'IRON_INGOT','Gold':'GOLD_INGOT','Diamond':'DIAMOND','Emerald':'EMERALD'}
    techOutputs={'Stone':'STONE','Iron':'IRON_BLOCK','Gold':'GOLD_BLOCK','Diamond':'DIAMOND_BLOCK','Emerald':'EMERALD_BLOCK'}
    for i in reversed(range(len(techLevels))):
        level=techLevels[i]
        recipes['E '+level]=Recipe('Efficient '+level+' Block',inputs=[ItemStack(techInputs[level])],outputs=[ItemStack(techOutputs[level])])
        recipes[level]=Recipe(level+' Block',inputs=[ItemStack(techInputs[level])],outputs=[ItemStack(techOutputs[level])])
        recipes['U E '+level]=Recipe('Upgrade to Efficient '+level+' Block',inputs=[ItemStack(techInputs[level])],outputRecipes=[recipes['E '+level]])
        if i!=len(techLevels)-1:
            outputRecipes=[recipes[level],recipes['U E '+level],recipes['U '+techLevels[i+1]]]
        else:
            outputRecipes=[recipes[level],recipes['U E '+level]]
        recipes['U '+level]=Recipe('Upgrade to '+level+' Block',inputs=[ItemStack(techInputs[level])],outputRecipes=outputRecipes)
    factories['Forge']=Factory('Forge','FORGE','COAL',fuelData=1)
    factories['Forge'].addRecipe(recipes['U Stone'])
    
    return {'factories':factories.values(),'recipes':recipes.values()}       
 
def createCauldron():
    factories={}
    recipes={}
    techs=['Clear','Diffuse','Artless','Thin','Bungling']
    outputs={'Clear':7,'Diffuse':11,'Artless':13,'Thin':14,'Bungling':23}
    inputs={}
    for tech in techs:
        inputs[tech]=[None,None]
    inputs[techs[0]][0]=[ItemStack('WHEAT'),ItemStack('COCOA')]
    inputs[techs[0]][1]=[ItemStack('POTATO'),ItemStack('BROWN_MUSHROOM')]
    inputs[techs[1]][0]=[ItemStack('BREAD'),ItemStack('COOKIE'),ItemStack('POTATO')]
    inputs[techs[1]][1]=[ItemStack('BAKED_POTATO'),ItemStack('MUSHROOM_SOUP'),ItemStack('WHEAT')]
    inputs[techs[2]][0]=[ItemStack('CAKE'),ItemStack('CHICKEN'),ItemStack('BAKED_POTATO')]
    inputs[techs[2]][1]=[ItemStack('PUMPKIN_PIE'),ItemStack('PORK'),ItemStack('BREAD')]
    inputs[techs[3]][0]=[ItemStack('CACTUS'),ItemStack('NETHERWART'),ItemStack('CAKE')]
    inputs[techs[3]][1]=[ItemStack('CACTUS'),ItemStack('NETHERWART'),ItemStack('PUMPKIN_PIE')]
    inputs[techs[4]][0]=[ItemStack('NETHERWART'),ItemStack('GOLDEN_APPLE'),ItemStack('STEAK')]
    inputs[techs[4]][1]=[ItemStack('NETHERWART'),ItemStack('GOLDEN_CARROT'),ItemStack('CAKE')]
    factories['Cauldron']=Factory('Cauldron','CAULDRON','COAL',fuelData=1)
    for i in reversed(range(len(techs))):
        tech=techs[i]
        for j in [0,1]:
            #Potion Recipe
            recipes[tech+' '+str(j)]=Recipe(tech+' Potion',inputs=inputs[tech][j],outputs=[ItemStack('POTION',data=outputs[tech])])
            #Upgrade Potion Station
            outputRecipes=[recipes[tech+' '+str(j)]]
            if i!=len(techs)-1:
                outputRecipes.append(recipes['U '+techs[i+1]+' '+str(j)])
            recipes['U '+tech+' '+str(j)]=Recipe('Upgrade to '+tech+' Potion',outputRecipes=outputRecipes)
    factories['Cauldron'].addRecipe(recipes['U '+techs[0]+' 0']) 
    factories['Cauldron'].addRecipe(recipes['U '+techs[0]+' 1'])
    
    return {'factories':factories.values(),'recipes':recipes.values()}    
 

    
if __name__ == '__main__':
    main()